package net.maomaocloud.maomaomusic.music.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.arbjerg.lavalink.client.player.Track;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.discord.model.DiscordServer;
import net.maomaocloud.maomaomusic.discord.service.DiscordServerService;
import net.maomaocloud.maomaomusic.discord.service.DiscordUserService;
import net.maomaocloud.maomaomusic.music.dto.*;
import net.maomaocloud.maomaomusic.music.model.*;
import net.maomaocloud.maomaomusic.music.repositories.BotSessionRepository;
import net.maomaocloud.maomaomusic.music.repositories.TrackPlaybackSessionRepository;
import net.maomaocloud.maomaomusic.music.repositories.UserListeningSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);

    private final SongService songService;
    private final DiscordUserService userService;
    private final BotSessionRepository botSessionRepository;
    private final TrackPlaybackSessionRepository trackPlaybackSessionRepository;
    private final UserListeningSessionRepository userListeningSessionRepository;
    private final DiscordServerService discordServerService;
    private final ObjectMapper objectMapper;

    private final Map<UUID, UUID> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    public StatsService(SongService songService,
                        DiscordUserService userService,
                        BotSessionRepository botSessionRepository,
                        TrackPlaybackSessionRepository trackPlaybackSessionRepository,
                        UserListeningSessionRepository userListeningSessionRepository,
                        DiscordServerService discordServerService,
                        ObjectMapper objectMapper) {
        this.songService = songService;
        this.userService = userService;
        this.botSessionRepository = botSessionRepository;
        this.trackPlaybackSessionRepository = trackPlaybackSessionRepository;
        this.userListeningSessionRepository = userListeningSessionRepository;
        this.discordServerService = discordServerService;
        this.objectMapper = objectMapper;
    }

    // Utility methods

    private String formatDurationToMinSec(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    public Optional<TrackPlaybackSession> getPlaybackSessionById(UUID id) {
        return trackPlaybackSessionRepository.findById(id);
    }

    private Optional<DiscordUser> getDiscordUserById(String userId) {
        return userService.getDiscordUser(Long.parseLong(userId));
    }

    private long getSessionDuration(UserListeningSession session) {
        return getPlaybackSessionById(session.getTrackPlaybackSessionId())
                .filter(track -> track.getStartedAt() != null && session.getLeftAt() != null)
                .map(track -> session.getLeftAt() - track.getStartedAt())
                .orElse(0L);
    }

    private Optional<DiscordUser> getUserFromSession(UserListeningSession session) {
        return getDiscordUserById(session.getUserId());
    }

    private Optional<DiscordUser> getRequesterFromPlaybackSession(TrackPlaybackSession session) {
        return getDiscordUserById(session.getRequesterId());
    }

    private Optional<SimpleSong> getSongFromPlaybackSession(TrackPlaybackSession session) {
        return songService.getSongById(session.getSongId());
    }

    // Session management

    public Optional<TrackPlaybackSession> markSessionStarted(UUID sessionId, List<String> listenerIds) {
        UUID actualSessionId = sessionMap.getOrDefault(sessionId, sessionId);

        Optional<TrackPlaybackSession> sessionOpt = getPlaybackSessionById(actualSessionId);
        if (sessionOpt.isPresent()) {
            TrackPlaybackSession session = sessionOpt.get();
            if (session.getStartedAt() == null) {
                session.setStartedAt(System.currentTimeMillis());
                listenerIds.forEach(session::addListenerId);

                listenerIds.forEach(userId -> {
                    UserListeningSession listeningSession = new UserListeningSession(
                            userId,
                            session.getGuildId(),
                            session.getVoiceChannelId().toString(),
                            actualSessionId,
                            null
                    );
                    userListeningSessionRepository.save(listeningSession);
                });

                LOGGER.info("Track started for session: {}", actualSessionId);
                return Optional.of(trackPlaybackSessionRepository.save(session));
            } else {
                LOGGER.warn("Session {} has already started at {}.", actualSessionId, session.getStartedAt());
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    public Optional<TrackPlaybackSession> markSessionEnded(UUID sessionId) {
        UUID actualSessionId = sessionMap.getOrDefault(sessionId, sessionId);

        Optional<TrackPlaybackSession> sessionOpt = getPlaybackSessionById(actualSessionId);
        if (sessionOpt.isPresent()) {
            TrackPlaybackSession session = sessionOpt.get();
            if (session.getEndedAt() == null) {
                long now = System.currentTimeMillis();
                session.setEndedAt(now);

                SimpleSong song = getSongFromPlaybackSession(session).orElse(null);
                String songTitle = song != null ? song.getTitle() : "Unknown";

                LOGGER.info("Track ended for session: {}. Song: {}", actualSessionId, songTitle);
                TrackPlaybackSession savedSession = trackPlaybackSessionRepository.save(session);

                List<UserListeningSession> userSessions = userListeningSessionRepository
                        .findByTrackPlaybackSessionIdAndLeftAtIsNull(actualSessionId);
                userSessions.forEach(userSession -> {
                    userSession.setLeftAt(now);
                    String duration = formatDurationToMinSec(getSessionDuration(userSession));
                    LOGGER.info("Marking session {} for song {} has ended. Lasted {}",
                            userSession.getId(),
                            songTitle,
                            duration);
                    userListeningSessionRepository.save(userSession);
                });

                // Create a new session in case of repeating songs
                TrackPlaybackSession futureSession = new TrackPlaybackSession();
                futureSession.setSongId(savedSession.getSongId());
                futureSession.setRequesterId(savedSession.getRequesterId());
                futureSession.setGuildId(savedSession.getGuildId());
                futureSession.setVoiceChannelId(savedSession.getVoiceChannelId());

                TrackPlaybackSession savedFutureSession = trackPlaybackSessionRepository.save(futureSession);

                sessionMap.put(sessionId, savedFutureSession.getId());

                return Optional.of(savedSession);
            } else {
                LOGGER.warn("Session {} has already ended at {}.", actualSessionId, session.getEndedAt());
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    public void clearUnstartedSessions(String guildId) {
        List<TrackPlaybackSession> unstartedSessions = trackPlaybackSessionRepository
                .findByGuildIdAndStartedAtIsNull(guildId);

        if (unstartedSessions.isEmpty()) {
            return;
        }

        Set<UUID> unstartedIds = unstartedSessions.stream()
                .map(TrackPlaybackSession::getId)
                .collect(Collectors.toSet());

        trackPlaybackSessionRepository.deleteAll(unstartedSessions);

        Iterator<Map.Entry<UUID,UUID>> iter = sessionMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID,UUID> entry = iter.next();
            UUID actualSessionId = entry.getValue();
            if (unstartedIds.contains(actualSessionId)) {
                iter.remove();
            }
        }
    }

    public Optional<TrackUserDetail> extractTrackUserDetail(Track track) {
        try {
            JsonNode userData = track.getUserData();
            return Optional.of(objectMapper.treeToValue(userData, TrackUserDetail.class));
        } catch (Exception e) {
            LOGGER.warn("Failed to extract track user detail", e);
        }
        return Optional.empty();
    }

    public TrackPlaybackSession createPlaybackSession(String songId,
                                                      String requestId,
                                                      String guildId,
                                                      Long channelId) {
        TrackPlaybackSession session = new TrackPlaybackSession();
        session.setSongId(songId);
        session.setRequesterId(requestId);
        session.setGuildId(guildId);
        session.setVoiceChannelId(channelId);
        return trackPlaybackSessionRepository.save(session);
    }

    public Track attachSessionIdToTrack(Track track, UUID sessionId) {
        try {
            TrackUserDetail userDetail = new TrackUserDetail(sessionId);
            track.setUserData(objectMapper.valueToTree(userDetail));
            return track;
        } catch (Exception e) {
            LOGGER.error("Failed to attach session ID to track", e);
            return track;
        }
    }

    public void handleUserLeftVoiceChannel(String userId, String guildId, String channelId) {
        List<UserListeningSession> sessions;
        if (userService.isBotUser(userId)) {
            sessions = userListeningSessionRepository
                    .findByGuildIdAndLeftAtIsNull(guildId);
        } else {
            sessions = userListeningSessionRepository
                    .findByUserIdAndGuildIdAndVoiceChannelIdAndLeftAtIsNull(userId, guildId, channelId);
        }

        long now = System.currentTimeMillis();
        sessions.forEach(session -> {
            session.setLeftAt(now);
            userListeningSessionRepository.save(session);
        });
    }

    public void saveBotSession(BotSession session) {
        botSessionRepository.save(session);
    }

    public List<BotSession> removeOrphanedBotSessions() {
        List<BotSession> orphans = botSessionRepository.findByStoppedAtIsNull();
        botSessionRepository.deleteAll(orphans);
        return orphans;
    }

    public List<TrackPlaybackSession> removeOrphanedPlaybackSessions() {
        List<TrackPlaybackSession> orphans = new ArrayList<>();
        orphans.addAll(trackPlaybackSessionRepository.findAllByEndedAtIsNull());
        orphans.addAll(trackPlaybackSessionRepository.findAllByStartedAtIsNull());
        trackPlaybackSessionRepository.deleteAll(orphans);
        return orphans;
    }

    public List<UserListeningSession> removeOrphanedListeningSessions() {
        List<UserListeningSession> orphans = userListeningSessionRepository.findByLeftAtIsNull();
        userListeningSessionRepository.deleteAll(orphans);
        return orphans;
    }

    // Query building

    public List<TrackPlaybackSession> getSessionsByGuild(String guildId, Instant from, Instant to) {
        return trackPlaybackSessionRepository.findByGuildIdAndStartedAtBetween(guildId, from.toEpochMilli(), to.toEpochMilli());
    }

    public List<TrackPlaybackSession> getSessionByUser(String userId) {
        return trackPlaybackSessionRepository.findByRequesterId(userId);
    }

    public List<UserListeningSession> getListeningSessionsByGuild(String guildId, Instant from, Instant to) {
        return userListeningSessionRepository.findByGuildIdAndLeftAtBetween(guildId, from.toEpochMilli(), to.toEpochMilli());
    }

    public List<UserListeningSession> getListeningSessionsByUser(String userId, Instant from, Instant to) {
        return userListeningSessionRepository.findByUserIdAndLeftAtBetween(userId, from.toEpochMilli(), to.toEpochMilli());
    }

    public long getTotalListeningTime(String userId, Instant from, Instant to) {
        return getListeningSessionsByUser(userId, from, to).stream()
                .mapToLong(session -> {
                    Optional<TrackPlaybackSession> playbackSession = getPlaybackSessionById(session.getTrackPlaybackSessionId());
                    if (playbackSession.isEmpty()) {
                        LOGGER.warn("No playback session found for listening session {}.", session.getId());
                        return 0;
                    }
                    Long getStartedAt = playbackSession.get().getStartedAt();
                    if (getStartedAt == null) {
                        LOGGER.warn("Playback session {} has no startedAt timestamp.", playbackSession.get().getId());
                        return 0;
                    }
                    return session.getLeftAt() - getStartedAt;
                })
                .sum();
    }

    public long getBotUptime(Instant from, Instant to) {
        return botSessionRepository.findByStartedAtBetween(from.toEpochMilli(), to.toEpochMilli()).stream()
                .mapToLong(session -> {
                    long end = session.getStoppedAt() != null ? session.getStoppedAt() : System.currentTimeMillis();
                    return end - session.getStartedAt();
                }).sum();
    }

    public Set<DiscordUser> getUniqueListeners(String guildId, Instant from, Instant to) {
        return getListeningSessionsByGuild(guildId, from, to).stream()
                .map(UserListeningSession::getUserId)
                .map(this::getDiscordUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }


    // Time utils

    public Instant getStartOfToday() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

    public Instant getStartOfWeek() {
        return LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

    public Instant getStartOfMonth() {
        return LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

    public Instant getStartOfYear() {
        return LocalDate.now()
                .withDayOfYear(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

    public Instant getNow() {
        return Instant.now();
    }

    // Statistics

    public PreciseStatDTO<DiscordUser> getTotalListeningTimeDTO(String userId, Instant from, Instant to) {
        long totalListeningTime = getTotalListeningTime(userId, from, to);
        DiscordUser user = getDiscordUserById(userId).orElse(null);
        return new PreciseStatDTO<>(user, totalListeningTime);
    }

    public PreciseStatDTO<DiscordUser> getTotalListeningTimeTodayDTO(String userId) {
        return getTotalListeningTimeDTO(userId, getStartOfToday(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getTotalListeningTimeThisWeekDTO(String userId) {
        return getTotalListeningTimeDTO(userId, getStartOfWeek(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getTotalListeningTimeThisMonthDTO(String userId) {
        return getTotalListeningTimeDTO(userId, getStartOfMonth(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getTotalListeningTimeThisYearDTO(String userId) {
        return getTotalListeningTimeDTO(userId, getStartOfYear(), getNow());
    }

    public Map<DiscordUser, Long> getGlobalTotalListeningTimeByUser(Instant from, Instant to) {
        return userListeningSessionRepository.findByLeftAtBetween(from.toEpochMilli(), to.toEpochMilli()).stream()
                .map(session -> getUserFromSession(session).map(user -> Map.entry(user, getSessionDuration(session))))
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopListenersDTO(Instant from, Instant to, int fromIndex, int toIndex) {
        Map<DiscordUser, Long> totalListeningTimes = getGlobalTotalListeningTimeByUser(from, to);

        return getTopRange(totalListeningTimes, fromIndex, toIndex).stream()
                .map(entry -> new TrackCountDTO<>(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopListenersTodayDTO(int from, int to) {
        return getGlobalTopListenersDTO(getStartOfToday(), getNow(), from, to);
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopListenersThisWeekDTO(int from, int to) {
        return getGlobalTopListenersDTO(getStartOfWeek(), getNow(), from, to);
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopListenersThisMonthDTO(int from, int to) {
        return getGlobalTopListenersDTO(getStartOfMonth(), getNow(), from, to);
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopListenersThisYearDTO(int from, int to) {
        return getGlobalTopListenersDTO(getStartOfYear(), getNow(), from, to);
    }

    public Map<DiscordUser, Long> getTotalListeningTimeByUser(String guildId, Instant from, Instant to) {
        return getListeningSessionsByGuild(guildId, from, to).stream()
                .map(session -> getUserFromSession(session).map(user -> Map.entry(user, getSessionDuration(session))))
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));
    }

    public List<TrackCountDTO<DiscordUser>> getTopListenersDTO(String guildId,
                                                               Instant fromInstant,
                                                               Instant toInstant,
                                                               int from,
                                                               int to) {

        Map<DiscordUser, Long> totalListeningTimes = getTotalListeningTimeByUser(guildId, fromInstant, toInstant);

        return getTopRange(totalListeningTimes, from, to).stream()
                .map(entry -> new TrackCountDTO<>(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<TrackCountDTO<DiscordUser>> getTopListenersTodayDTO(String guildId, int from, int to) {
        return getTopListenersDTO(guildId, getStartOfToday(), getNow(), from, to);
    }

    public List<TrackCountDTO<DiscordUser>> getTopListenersThisWeekDTO(String guildId, int from, int to) {
        return getTopListenersDTO(guildId, getStartOfWeek(), getNow(), from, to);
    }

    public List<TrackCountDTO<DiscordUser>> getTopListenersThisMonthDTO(String guildId, int from, int to) {
        return getTopListenersDTO(guildId, getStartOfMonth(), getNow(), from, to);
    }

    public List<TrackCountDTO<DiscordUser>> getTopListenersThisYearDTO(String guildId, int from, int to) {
        return getTopListenersDTO(guildId, getStartOfYear(), getNow(), from, to);
    }

    public Map<SimpleSong, Long> getTrackPlayCounts(String guildId, Instant from, Instant to) {
        return getSessionsByGuild(guildId, from, to).stream()
                .map(session -> songService.getSongById(session.getSongId())
                        .map(song -> Map.entry(song, 1L)))
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));
    }

    public double getAverageSessionLength(String userId, Instant from, Instant to) {
        List<UserListeningSession> sessions = getListeningSessionsByUser(userId, from, to);
        if (sessions.isEmpty()) return 0;

        long total = sessions.stream()
                .mapToLong(this::getSessionDuration)
                .sum();

        return (double) total / sessions.size();
    }

    public PreciseStatDTO<DiscordUser> getAverageSessionLengthDTO(String userId, Instant from, Instant to) {
        double averageSessionLength = getAverageSessionLength(userId, from, to);
        DiscordUser user = getDiscordUserById(userId).orElse(null);
        return new PreciseStatDTO<>(user, averageSessionLength);
    }

    public PreciseStatDTO<DiscordUser> getAverageSessionLengthTodayDTO(String userId) {
        return getAverageSessionLengthDTO(userId, getStartOfToday(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getAverageSessionLengthThisWeekDTO(String userId) {
        return getAverageSessionLengthDTO(userId, getStartOfWeek(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getAverageSessionLengthThisMonthDTO(String userId) {
        return getAverageSessionLengthDTO(userId, getStartOfMonth(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getAverageSessionLengthThisYearDTO(String userId) {
        return getAverageSessionLengthDTO(userId, getStartOfYear(), getNow());
    }

    public double getMedianSessionLength(String userId, Instant from, Instant to) {
        List<Long> durations = getListeningSessionsByUser(userId, from, to).stream()
                .map(this::getSessionDuration)
                .sorted()
                .toList();

        int size = durations.size();
        if (size == 0) return 0;

        return (size % 2 == 0)
                ? (durations.get(size / 2 - 1) + durations.get(size / 2)) / 2.0
                : durations.get(size / 2);
    }


    public PreciseStatDTO<DiscordUser> getMedianSessionLengthDTO(String userId, Instant from, Instant to) {
        double medianSessionLength = getMedianSessionLength(userId, from, to);
        DiscordUser user = getDiscordUserById(userId).orElse(null);
        return new PreciseStatDTO<>(user, medianSessionLength);
    }

    public PreciseStatDTO<DiscordUser> getMedianSessionLengthTodayDTO(String userId) {
        return getMedianSessionLengthDTO(userId, getStartOfToday(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getMedianSessionLengthThisWeekDTO(String userId) {
        return getMedianSessionLengthDTO(userId, getStartOfWeek(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getMedianSessionLengthThisMonthDTO(String userId) {
        return getMedianSessionLengthDTO(userId, getStartOfMonth(), getNow());
    }

    public PreciseStatDTO<DiscordUser> getMedianSessionLengthThisYearDTO(String userId) {
        return getMedianSessionLengthDTO(userId, getStartOfYear(), getNow());
    }

    public Map<DiscordUser, Long> getPlaybackCountByUser(String guildId, Instant from, Instant to) {
        return getSessionsByGuild(guildId, from, to).stream()
                .map(session -> getRequesterFromPlaybackSession(session).map(user -> Map.entry(user, 1L)))
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));
    }

    public Map<LocalDate, Long> getDailySessionCount(String guildId, Instant from, Instant to) {
        return getSessionsByGuild(guildId, from, to).stream()
                .filter(session -> session.getStartedAt() != null)
                .collect(Collectors.groupingBy(
                        session -> Instant.ofEpochMilli(session.getStartedAt())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                        Collectors.counting()
                ));
    }

    public List<DailyStatDTO> getDailySessionCountDTO(String guildId, Instant from, Instant to) {
        return getDailySessionCount(guildId, from, to).entrySet().stream()
                .map(entry -> new DailyStatDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<DailyStatDTO> getDailySessionCountDTO(String guildId, LocalDate date) {
        return getDailySessionCountDTO(
                guildId,
                date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
    }

    public List<Map.Entry<DiscordUser, Long>> getGlobalTopRequesters(Instant from, Instant to, int limit) {
        Map<DiscordUser, Long> counts = trackPlaybackSessionRepository.findAllByStartedAtBetween(from.toEpochMilli(), to.toEpochMilli()).stream()
                .map(session -> getRequesterFromPlaybackSession(session).map(user -> Map.entry(user, 1L)))
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));

        return getTopN(counts, limit);
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopRequestersDTO(Instant from, Instant to, int limit) {
        return getGlobalTopRequesters(from, to, limit).stream()
                .map(entry -> new TrackCountDTO<>(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopRequestersTodayDTO(int limit) {
        return getGlobalTopRequestersDTO(getStartOfToday(), getNow(), limit);
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopRequestersThisWeekDTO(int limit) {
        return getGlobalTopRequestersDTO(getStartOfWeek(), getNow(), limit);
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopRequestersThisMonthDTO(int limit) {
        return getGlobalTopRequestersDTO(getStartOfMonth(), getNow(), limit);
    }

    public List<TrackCountDTO<DiscordUser>> getGlobalTopRequestersThisYearDTO(int limit) {
        return getGlobalTopRequestersDTO(getStartOfYear(), getNow(), limit);
    }

    public List<Map.Entry<DiscordUser, Long>> getTopRequesters(String guildId, Instant from, Instant to, int limit) {
        return getPlaybackCountByUser(guildId, from, to).entrySet().stream()
                .sorted(Map.Entry.<DiscordUser, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<TrackCountDTO<DiscordUser>> getTopRequestersDTO(String guildId, Instant from, Instant to, int limit) {
        return getTopRequesters(guildId, from, to, limit).stream()
                .map(entry -> new TrackCountDTO<>(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<TrackCountDTO<DiscordUser>> getTopRequestersTodayDTO(String guildId, int limit) {
        return getTopRequestersDTO(guildId, getStartOfToday(), getNow(), limit);
    }

    public List<TrackCountDTO<DiscordUser>> getTopRequestersThisWeekDTO(String guildId, int limit) {
        return getTopRequestersDTO(guildId, getStartOfWeek(), getNow(), limit);
    }

    public List<TrackCountDTO<DiscordUser>> getTopRequestersThisMonthDTO(String guildId, int limit) {
        return getTopRequestersDTO(guildId, getStartOfMonth(), getNow(), limit);
    }

    public List<TrackCountDTO<DiscordUser>> getTopRequestersThisYearDTO(String guildId, int limit) {
        return getTopRequestersDTO(guildId, getStartOfYear(), getNow(), limit);
    }

    public Map<LocalDate, Long> getDailyListeningTime(String guildId, Instant from, Instant to) {
        return getListeningSessionsByGuild(guildId, from, to).stream()
                .collect(Collectors.groupingBy(
                        session -> Instant.ofEpochMilli(session.getLeftAt())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                        Collectors.summingLong(this::getSessionDuration)
                ));
    }

    public List<DailyStatDTO> getDailyListeningTimeDTO(String guildId, Instant from, Instant to) {
        return getDailyListeningTime(guildId, from, to).entrySet().stream()
                .map(entry -> new DailyStatDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<DailyStatDTO> getDailyListeningTimeDTO(String guildId, LocalDate date) {
        return getDailyListeningTimeDTO(
                guildId,
                date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
    }

    public Map<Integer, Long> getListeningTimeByHour(String guildId, Instant from, Instant to) {
        return getListeningSessionsByGuild(guildId, from, to).stream()
                .collect(Collectors.groupingBy(
                        session -> Instant.ofEpochMilli(session.getLeftAt())
                                .atZone(ZoneId.systemDefault())
                                .getHour(),
                        Collectors.summingLong(this::getSessionDuration)
                ));
    }

    public List<HourStatDTO> getListeningTimeByHourDTO(String guildId, Instant from, Instant to) {
        return getListeningTimeByHour(guildId, from, to).entrySet().stream()
                .map(entry -> new HourStatDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<HourStatDTO> getListeningTimeByHourDTO(String guildId, LocalDate date) {
        return getListeningTimeByHourDTO(
                guildId,
                date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
    }

    public long getUniqueTrackCount(String guildId, Instant from, Instant to) {
        return getSessionsByGuild(guildId, from, to).stream()
                .map(TrackPlaybackSession::getSongId)
                .distinct()
                .count();
    }

    public TrackCountDTO<DiscordServer> getUniqueTrackCountDTO(String guildId, Instant from, Instant to) {
        long uniqueTrackCount = getUniqueTrackCount(guildId, from, to);
        DiscordServer guild = discordServerService.getDiscordServerById(guildId).orElse(null);
        return new TrackCountDTO<>(guild, uniqueTrackCount);
    }

    public TrackCountDTO<DiscordServer> getUniqueTrackCountTodayDTO(String guildId) {
        return getUniqueTrackCountDTO(guildId, getStartOfToday(), getNow());
    }

    public TrackCountDTO<DiscordServer> getUniqueTrackCountThisWeekDTO(String guildId) {
        return getUniqueTrackCountDTO(guildId, getStartOfWeek(), getNow());
    }

    public TrackCountDTO<DiscordServer> getUniqueTrackCountThisMonthDTO(String guildId) {
        return getUniqueTrackCountDTO(guildId, getStartOfMonth(), getNow());
    }

    public TrackCountDTO<DiscordServer> getUniqueTrackCountThisYearDTO(String guildId) {
        return getUniqueTrackCountDTO(guildId, getStartOfYear(), getNow());
    }

    public double getAverageBotUptimePerDay(Instant from, Instant to) {
        long uptime = getBotUptime(from, to);
        long days = Math.max(1, Duration.between(from, to).toDays());
        return (double) uptime / days;
    }

    public PreciseStatDTO<LocalDate> getAverageBotUptimeTodayDTO() {
        return new PreciseStatDTO<>(LocalDate.now(), getAverageBotUptimePerDay(getStartOfToday(), getNow()));
    }

    public PreciseStatDTO<LocalDate> getAverageBotUptimeThisWeekDTO() {
        LocalDate startOfWeek = getStartOfWeek().atZone(ZoneId.systemDefault()).toLocalDate();
        return new PreciseStatDTO<>(startOfWeek, getAverageBotUptimePerDay(getStartOfWeek(), getNow()));
    }


    public PreciseStatDTO<LocalDate> getAverageBotUptimeThisMonthDTO() {
        LocalDate startOfMonth = getStartOfMonth().atZone(ZoneId.systemDefault()).toLocalDate();
        return new PreciseStatDTO<>(startOfMonth, getAverageBotUptimePerDay(getStartOfMonth(), getNow()));
    }

    public PreciseStatDTO<LocalDate> getAverageBotUptimeThisYearDTO() {
        LocalDate startOfYear = getStartOfYear().atZone(ZoneId.systemDefault()).toLocalDate();
        return new PreciseStatDTO<>(startOfYear, getAverageBotUptimePerDay(getStartOfYear(), getNow()));
    }

    public Map<SimpleSong, Long> getTrackPlayCountsToday(String guildId) {
        return getTrackPlayCounts(guildId, getStartOfToday(), getNow());
    }

    public List<SongStatDTO> getTrackPlayCountsTodayDTO(String guildId) {
        return mapToSongStatDTO(getTrackPlayCountsToday(guildId));
    }

    public Map<SimpleSong, Long> getTrackPlayCountsThisWeek(String guildId) {
        return getTrackPlayCounts(guildId, getStartOfWeek(), getNow());
    }

    public List<SongStatDTO> getTrackPlayCountsThisWeekDTO(String guildId) {
        return mapToSongStatDTO(getTrackPlayCountsThisWeek(guildId));
    }

    public Map<SimpleSong, Long> getTrackPlayCountsThisMonth(String guildId) {
        return getTrackPlayCounts(guildId, getStartOfMonth(), getNow());
    }

    public List<SongStatDTO> getTrackPlayCountsThisMonthDTO(String guildId) {
        return mapToSongStatDTO(getTrackPlayCountsThisMonth(guildId));
    }

    public Map<SimpleSong, Long> getTrackPlayCountsThisYear(String guildId) {
        return getTrackPlayCounts(guildId, getStartOfYear(), getNow());
    }

    public List<SongStatDTO> getTrackPlayCountsThisYearDTO(String guildId) {
        return mapToSongStatDTO(getTrackPlayCountsThisYear(guildId));
    }

    public Map<SimpleSong, Long> getGlobalTrackCount(Instant from, Instant to) {
        return trackPlaybackSessionRepository.findByStartedAtBetween(from.toEpochMilli(), to.toEpochMilli()).stream()
                .map(session -> getSongFromPlaybackSession(session).map(song -> Map.entry(song, 1L)))
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingLong(Map.Entry::getValue)
                ));
    }

    public Map<SimpleSong, Long> getGlobalTrackCountForToday() {
        return getGlobalTrackCount(getStartOfToday(), getNow());
    }

    public List<SongStatDTO> getGlobalTrackCountForTodayDTO() {
        return mapToSongStatDTO(getGlobalTrackCountForToday());
    }

    public Map<SimpleSong, Long> getGlobalTrackCountThisWeek() {
        return getGlobalTrackCount(getStartOfWeek(), getNow());
    }

    public List<SongStatDTO> getGlobalTrackCountThisWeekDTO() {
        return mapToSongStatDTO(getGlobalTrackCountThisWeek());
    }

    public Map<SimpleSong, Long> getGlobalTrackCountThisMonth() {
        return getGlobalTrackCount(getStartOfMonth(), getNow());
    }

    public List<SongStatDTO> getGlobalTrackCountThisMonthDTO() {
        return mapToSongStatDTO(getGlobalTrackCountThisMonth());
    }

    public Map<SimpleSong, Long> getGlobalTrackCountThisYear() {
        return getGlobalTrackCount(getStartOfYear(), getNow());
    }

    public List<SongStatDTO> getGlobalTrackCountThisYearDTO() {
        return mapToSongStatDTO(getGlobalTrackCountThisYear());
    }

    public List<Map.Entry<SimpleSong, Long>> getTopTracks(
            String guildId,
            Instant fromInstant,
            Instant toInstant,
            int from,
            int to) {
        Map<SimpleSong, Long> playCounts = getTrackPlayCounts(guildId, fromInstant, toInstant);

        int safeFrom = Math.max(0, from);
        int safeTo = Math.max(safeFrom, to);

        return getTopRange(playCounts, safeFrom, safeTo);
    }


    public List<SongStatDTO> getTopTracksDTO(
            String guildId,
            Instant fromInstant,
            Instant toInstant,
            int from,
            int to) {
        return getTopTracks(guildId, fromInstant, toInstant, from, to).stream()
                .map(entry -> new SongStatDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<SongStatDTO> getTopTracksTodayDTO(String guildId, int from, int to) {
        return getTopTracksDTO(guildId, getStartOfToday(), getNow(), from, to);
    }

    public List<SongStatDTO> getTopTracksThisWeekDTO(String guildId, int from, int to) {
        return getTopTracksDTO(guildId, getStartOfWeek(), getNow(), from, to);
    }

    public List<SongStatDTO> getTopTracksThisMonthDTO(String guildId, int from, int to) {
        return getTopTracksDTO(guildId, getStartOfMonth(), getNow(), from, to);
    }

    public List<SongStatDTO> getTopTracksThisYearDTO(String guildId, int from, int to) {
        return getTopTracksDTO(guildId, getStartOfYear(), getNow(), from, to);
    }

    public List<Map.Entry<SimpleSong, Long>> getGlobalTopTracks(
            Instant fromInstant,
            Instant toInstant,
            int from,
            int to) {

        Map<SimpleSong, Long> playCounts = getGlobalTrackCount(fromInstant, toInstant);

        int safeFrom = Math.max(0, from);
        int safeTo = Math.max(safeFrom, to);

        return getTopRange(playCounts, safeFrom, safeTo);
    }

    public List<SongStatDTO> getGlobalTopTracksDTO(
            Instant fromInstant,
            Instant toInstant,
            int from,
            int to) {

        List<Map.Entry<SimpleSong, Long>> topEntries = getGlobalTopTracks(fromInstant, toInstant, from, to);

        return topEntries.stream()
                .map(entry -> new SongStatDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<SongStatDTO> getGlobalTopTracksTodayDTO(int from, int to) {
        return getGlobalTopTracksDTO(getStartOfToday(), getNow(), from, to);
    }

    public List<SongStatDTO> getGlobalTopTracksThisWeekDTO(int from, int to) {
        return getGlobalTopTracksDTO(getStartOfWeek(), getNow(), from, to);
    }

    public List<SongStatDTO> getGlobalTopTracksThisMonthDTO(int from, int to) {
        return getGlobalTopTracksDTO(getStartOfMonth(), getNow(), from, to);
    }

    public List<SongStatDTO> getGlobalTopTracksThisYearDTO(int from, int to) {
        return getGlobalTopTracksDTO(getStartOfYear(), getNow(), from, to);
    }


    public <I, O extends Comparable<? super O>> List<Map.Entry<I, O>> getTopN(Map<I, O> map, int n) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<I, O>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public <I, O extends Comparable<? super O>> List<Map.Entry<I, O>> getTopRange(Map<I, O> map, int from, int to) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<I, O>comparingByValue().reversed())
                .skip(from)
                .limit(to - from)
                .collect(Collectors.toList());
    }

    private List<SongStatDTO> mapToSongStatDTO(Map<SimpleSong, Long> map) {
        return map.entrySet().stream()
                .map(entry -> new SongStatDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<TrackCountDTO<DiscordUser>> mapToUserStatDTO(Map<DiscordUser, Long> map) {
        return map.entrySet().stream()
                .map(entry -> new TrackCountDTO<>(entry.getKey(), entry.getValue()))
                .toList();
    }

}

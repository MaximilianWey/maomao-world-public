package net.maomaocloud.maomaomusic.music.controller;

import net.maomaocloud.maomaomusic.discord.service.DiscordInfoService;
import net.maomaocloud.maomaomusic.discord.model.DiscordUser;
import net.maomaocloud.maomaomusic.discord.service.DiscordUserService;
import net.maomaocloud.maomaomusic.music.dto.*;
import net.maomaocloud.maomaomusic.discord.model.DiscordServer;
import net.maomaocloud.maomaomusic.discord.service.DiscordServerService;
import net.maomaocloud.maomaomusic.music.service.StatsService;
import net.maomaocloud.maomaomusic.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/discord/stats")
public class DiscordStatsController {

    private final StatsService statsService;
    private final DiscordInfoService discordInfoService;
    private final DiscordServerService discordServerService;
    private final DiscordUserService discordUserService;

    @Autowired
    public DiscordStatsController(StatsService statsService,
                                  DiscordInfoService discordInfoService,
                                  DiscordServerService discordServerService, DiscordUserService discordUserService) {
        this.statsService = statsService;
        this.discordInfoService = discordInfoService;
        this.discordServerService = discordServerService;
        this.discordUserService = discordUserService;
    }

    @GetMapping("/global/track-count")
    public ResponseEntity<?> getGlobalTrackCount(@RequestParam String range,
                                               @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            List<SongStatDTO> topSongs;
            switch (range.toLowerCase()) {
                case "today" -> topSongs = statsService.getGlobalTrackCountForTodayDTO();
                case "week" -> topSongs = statsService.getGlobalTrackCountThisWeekDTO();
                case "month" -> topSongs = statsService.getGlobalTrackCountThisMonthDTO();
                case "year" -> topSongs = statsService.getGlobalTrackCountThisYearDTO();
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topSongs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{guildId}/track-count")
    public ResponseEntity<?> getTrackCount(@PathVariable String guildId,
                                           @RequestParam String range,
                                           @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            Optional<DiscordServer> guildOpt = discordServerService.getDiscordServerById(guildId);
            if (guildOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guild not found");
            }

            List<SongStatDTO> topSongs;
            switch (range.toLowerCase()) {
                case "today" -> topSongs = statsService.getTrackPlayCountsTodayDTO(guildId);
                case "week" -> topSongs = statsService.getTrackPlayCountsThisWeekDTO(guildId);
                case "month" -> topSongs = statsService.getTrackPlayCountsThisMonthDTO(guildId);
                case "year" -> topSongs = statsService.getTrackPlayCountsThisYearDTO(guildId);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topSongs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{guildId}/top-tracks")
    public ResponseEntity<?> getTopTracks(@PathVariable String guildId,
                                          @RequestParam int from,
                                          @RequestParam int to,
                                          @RequestParam String range,
                                          @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            Optional<DiscordServer> guildOpt = discordServerService.getDiscordServerById(guildId);
            if (guildOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guild not found");
            }
            DiscordServer _ = guildOpt.get();

            List<SongStatDTO> topSongs;
            switch (range.toLowerCase()) {
                case "today" -> topSongs = statsService.getTopTracksTodayDTO(guildId, from, to);
                case "week" -> topSongs = statsService.getTopTracksThisWeekDTO(guildId, from, to);
                case "month" -> topSongs = statsService.getTopTracksThisMonthDTO(guildId, from, to);
                case "year" -> topSongs = statsService.getTopTracksThisYearDTO(guildId, from, to);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topSongs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/global/top-tracks")
    public ResponseEntity<?> getGlobalTopTracks(@RequestParam String range,
                                                @RequestParam int from,
                                                @RequestParam int to,
                                                @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            List<SongStatDTO> topSongs;
            switch (range.toLowerCase()) {
                case "today" -> topSongs = statsService.getGlobalTopTracksTodayDTO(from, to);
                case "week" -> topSongs = statsService.getGlobalTopTracksThisWeekDTO(from, to);
                case "month" -> topSongs = statsService.getGlobalTopTracksThisMonthDTO(from, to);
                case "year" -> topSongs = statsService.getGlobalTopTracksThisYearDTO(from, to);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topSongs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/global/top-listeners")
    public ResponseEntity<?> getGlobalTopListeners(@RequestParam String range,
                                                   @RequestParam int from,
                                                   @RequestParam int to,
                                                   @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            List<TrackCountDTO<DiscordUser>> topListeners;
            switch (range.toLowerCase()) {
                case "today" -> topListeners = statsService.getGlobalTopListenersTodayDTO(from, to);
                case "week" -> topListeners = statsService.getGlobalTopListenersThisWeekDTO(from, to);
                case "month" -> topListeners = statsService.getGlobalTopListenersThisMonthDTO(from, to);
                case "year" -> topListeners = statsService.getGlobalTopListenersThisYearDTO(from, to);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topListeners);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{guildId}/top-listeners")
    public ResponseEntity<?> getTopListeners(@PathVariable String guildId,
                                             @RequestParam int from,
                                             @RequestParam int to,
                                             @RequestParam String range,
                                             @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            Optional<DiscordServer> guildOpt = discordServerService.getDiscordServerById(guildId);
            if (guildOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guild not found");
            }
            DiscordServer _ = guildOpt.get();

            List<TrackCountDTO<DiscordUser>> topListeners;
            switch (range.toLowerCase()) {
                case "today" -> topListeners = statsService.getTopListenersTodayDTO(guildId, from, to);
                case "week" -> topListeners = statsService.getTopListenersThisWeekDTO(guildId, from, to);
                case "month" -> topListeners = statsService.getTopListenersThisMonthDTO(guildId, from, to);
                case "year" -> topListeners = statsService.getTopListenersThisYearDTO(guildId, from, to);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topListeners);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }


    @GetMapping("/global/top-requesters")
    public ResponseEntity<?> getGlobalTopRequesters(@RequestParam String range,
                                                    @RequestParam(required = false) Integer limit,
                                                    @AuthenticationPrincipal Jwt jwt) {
        try {
            Long discordId = JwtUtils.getDiscordId(jwt);

            int limitValue = (limit != null && limit > 0) ? limit : 10;

            List<TrackCountDTO<DiscordUser>> topRequesters;
            switch (range.toLowerCase()) {
                case "today" -> topRequesters = statsService.getGlobalTopRequestersTodayDTO(limitValue);
                case "week" -> topRequesters = statsService.getGlobalTopRequestersThisWeekDTO(limitValue);
                case "month" -> topRequesters = statsService.getGlobalTopRequestersThisMonthDTO(limitValue);
                case "year" -> topRequesters = statsService.getGlobalTopRequestersThisYearDTO(limitValue);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topRequesters);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }


    @GetMapping("/{guildId}/top-requesters")
    public ResponseEntity<?> getTopRequesters(@PathVariable String guildId,
                                              @RequestParam String range,
                                              @RequestParam(required = false) Integer limit,
                                              @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            Optional<DiscordServer> guildOpt = discordServerService.getDiscordServerById(guildId);
            if (guildOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guild not found");
            }
            DiscordServer _ = guildOpt.get();
            int limitValue = (limit != null && limit > 0) ? limit : 10;

            List<TrackCountDTO<DiscordUser>> topRequesters;
            switch (range.toLowerCase()) {
                case "today" -> topRequesters = statsService.getTopRequestersTodayDTO(guildId, limitValue);
                case "week" -> topRequesters = statsService.getTopRequestersThisWeekDTO(guildId, limitValue);
                case "month" -> topRequesters = statsService.getTopRequestersThisMonthDTO(guildId, limitValue);
                case "year" -> topRequesters = statsService.getTopRequestersThisYearDTO(guildId, limitValue);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(topRequesters);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/avg-session-length")
    public ResponseEntity<?> getAverageSessionLength(@PathVariable String userId,
                                                     @RequestParam String range,
                                                     @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(userId, jwt);

            Long discordId = JwtUtils.getDiscordId(jwt);

            Optional<DiscordUser> userOpt = discordUserService.getDiscordUser(discordId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            String userIdString = userOpt.get().getId();

            PreciseStatDTO<DiscordUser> averageSessionLength;
            switch (range.toLowerCase()) {
                case "today" -> averageSessionLength = statsService.getAverageSessionLengthTodayDTO(userIdString);
                case "week" -> averageSessionLength = statsService.getAverageSessionLengthThisWeekDTO(userIdString);
                case "month" -> averageSessionLength = statsService.getAverageSessionLengthThisMonthDTO(userIdString);
                case "year" -> averageSessionLength = statsService.getAverageSessionLengthThisYearDTO(userIdString);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(averageSessionLength);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/median-session-length")
    public ResponseEntity<?> getMedianSessionLength(@PathVariable String userId,
                                                    @RequestParam String range,
                                                    @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(userId, jwt);

            Long discordId = JwtUtils.getDiscordId(jwt);

            Optional<DiscordUser> userOpt = discordUserService.getDiscordUser(discordId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            String userIdString = userOpt.get().getId();

            PreciseStatDTO<DiscordUser> medianSessionLength;
            switch (range.toLowerCase()) {
                case "today" -> medianSessionLength = statsService.getMedianSessionLengthTodayDTO(userIdString);
                case "week" -> medianSessionLength = statsService.getMedianSessionLengthThisWeekDTO(userIdString);
                case "month" -> medianSessionLength = statsService.getMedianSessionLengthThisMonthDTO(userIdString);
                case "year" -> medianSessionLength = statsService.getMedianSessionLengthThisYearDTO(userIdString);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(medianSessionLength);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/user-session-length")
    public ResponseEntity<?> getSessionLength(@PathVariable String userId,
                                              @RequestParam String range,
                                              @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(userId, jwt);

            Long discordId = JwtUtils.getDiscordId(jwt);

            Optional<DiscordUser> userOpt = discordUserService.getDiscordUser(discordId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            String userIdString = userOpt.get().getId();

            PreciseStatDTO<DiscordUser> sessionLength;
            switch (range.toLowerCase()) {
                case "today" -> sessionLength = statsService.getTotalListeningTimeTodayDTO(userIdString);
                case "week" -> sessionLength = statsService.getTotalListeningTimeThisWeekDTO(userIdString);
                case "month" -> sessionLength = statsService.getTotalListeningTimeThisMonthDTO(userIdString);
                case "year" -> sessionLength = statsService.getTotalListeningTimeThisYearDTO(userIdString);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(sessionLength);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{guildId}/session-length")
    public ResponseEntity<?> getDailySessionLength(@PathVariable String guildId,
                                                   @RequestParam LocalDate date,
                                                   @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            DiscordServer _ = discordServerService.getDiscordServerById(guildId)
                    .orElseThrow(() -> new RuntimeException("Guild not found"));

            List<DailyStatDTO> dailySessionLength = statsService.getDailyListeningTimeDTO(guildId, date);
            return ResponseEntity.ok(dailySessionLength);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{guildId}/session-count")
    public ResponseEntity<?> getDailySessionCount(@PathVariable String guildId,
                                                  @RequestParam LocalDate date,
                                                  @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            DiscordServer _ = discordServerService.getDiscordServerById(guildId)
                    .orElseThrow(() -> new RuntimeException("Guild not found"));

            List<DailyStatDTO> dailySessionCount = statsService.getDailySessionCountDTO(guildId, date);
            return ResponseEntity.ok(dailySessionCount);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{guildId}/session-time-by-hour")
    public ResponseEntity<?> getDailySessionTimeByHour(@PathVariable String guildId,
                                                  @RequestParam LocalDate date,
                                                  @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            DiscordServer _ = discordServerService.getDiscordServerById(guildId)
                    .orElseThrow(() -> new RuntimeException("Guild not found"));

            List<HourStatDTO> sessionTimeByHour = statsService.getListeningTimeByHourDTO(guildId, date);
            return ResponseEntity.ok(sessionTimeByHour);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{guildId}/unique-track-count")
    public ResponseEntity<?> getUniqueTrackCount(@PathVariable String guildId,
                                                 @RequestParam String range,
                                                 @AuthenticationPrincipal Jwt jwt) {
        try {
            verifyAccess(guildId, jwt);

            DiscordServer _ = discordServerService.getDiscordServerById(guildId)
                    .orElseThrow(() -> new RuntimeException("Guild not found"));

            TrackCountDTO<DiscordServer> uniqueTrackCount;
            switch (range.toLowerCase()) {
                case "today" -> uniqueTrackCount = statsService.getUniqueTrackCountTodayDTO(guildId);
                case "week" -> uniqueTrackCount = statsService.getUniqueTrackCountThisWeekDTO(guildId);
                case "month" -> uniqueTrackCount = statsService.getUniqueTrackCountThisMonthDTO(guildId);
                case "year" -> uniqueTrackCount = statsService.getUniqueTrackCountThisYearDTO(guildId);
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(uniqueTrackCount);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/global/bot-uptime")
    public ResponseEntity<?> getGlobalBotUptime(@RequestParam String range,
                                                @AuthenticationPrincipal Jwt jwt) {
        try {
            PreciseStatDTO<LocalDate> botUptime;

            switch (range.toLowerCase()) {
                case "today" -> botUptime = statsService.getAverageBotUptimeTodayDTO();
                case "week" -> botUptime = statsService.getAverageBotUptimeThisWeekDTO();
                case "month" -> botUptime = statsService.getAverageBotUptimeThisMonthDTO();
                case "year" -> botUptime = statsService.getAverageBotUptimeThisYearDTO();
                default -> {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid range (today | week | month | year)");
                }
            }
            return ResponseEntity.ok(botUptime);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }

    private void verifyAccess(String guildId, Jwt jwt) throws RuntimeException {
        discordInfoService.verifyAccess(guildId, jwt);
    }

}

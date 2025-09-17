//package net.maomaocloud.authservice.api.openfga;
//
//import net.maomaocloud.authservice.api.openfga.relations.RelationType;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.expression.MethodBasedEvaluationContext;
//import org.springframework.core.DefaultParameterNameDiscoverer;
//import org.springframework.expression.EvaluationContext;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//
//@Aspect
//@Component
//public class AuthorizationAspect {
//
//    private final AuthorizationService service;
//    private final SpelExpressionParser parser;
//
//    @Autowired
//    public AuthorizationAspect(AuthorizationService service) {
//        this.service = service;
//        this.parser = new SpelExpressionParser();
//    }
//
//    @Around("@annotation(requirePermission)")
//    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String userId = ((Jwt) auth.getPrincipal()).getSubject();
//
//        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
//        Object[] args = joinPoint.getArgs();
//
//        EvaluationContext evaluationContext = new MethodBasedEvaluationContext(null, method, args, new DefaultParameterNameDiscoverer());
//        String resolvedObject = parser.parseExpression(requirePermission.object()).getValue(evaluationContext, String.class);
//
//        Class<? extends RelationType> clazz = requirePermission.relationClass();
//        RelationType relationEnum = Enum.valueOf(clazz.asSubclass(Enum.class), requirePermission.relation());
//
//        boolean allowed = service.checkAccess(userId, resolvedObject, relationEnum.getValue());
//        if (!allowed) throw new AccessDeniedException("Not authorized");
//
//        return joinPoint.proceed();
//     }
//}

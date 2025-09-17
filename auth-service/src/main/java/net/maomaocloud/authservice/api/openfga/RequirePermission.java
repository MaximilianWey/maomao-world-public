package net.maomaocloud.authservice.api.openfga;

import net.maomaocloud.authservice.api.openfga.relations.RelationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequirePermission {
    Class<? extends RelationType> relationClass();
    String relation();
    String object();
}

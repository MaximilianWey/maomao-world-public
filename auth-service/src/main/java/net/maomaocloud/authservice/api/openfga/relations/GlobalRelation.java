package net.maomaocloud.authservice.api.openfga.relations;

public enum GlobalRelation implements RelationType {
    ADMIN("admin"),
    MEMBER("member"),
    OWNER("owner"),
    GUEST("guest");

    private final String value;

    GlobalRelation(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}

package net.maomaocloud.authservice.api.openfga.relations;

public enum ServiceRelation implements RelationType {
    CAN_VIEW("can_view"),
    CAN_MANAGE("can_manage");

    private final String value;

    ServiceRelation(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}

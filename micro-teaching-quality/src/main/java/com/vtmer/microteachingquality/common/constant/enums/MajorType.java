package com.vtmer.microteachingquality.common.constant.enums;

/**
 * @author Colin_Knight
 * @create 2023/10/19 15:16
 */
public enum MajorType {


    ENGINEERING_COURSE("工学"),
    NEO_CONFUCIANISM("理学"),
    ART("艺术"),
    HUMANITIES("人文社科"),
    MANAGEMENT("管理学"),
    ECONOMICS("经济学");
    private final String majorType;


    MajorType(String value) {
        this.majorType = value;
    }


    public static boolean isHUMANITIES(String majorType) {
        return majorType.equals(MANAGEMENT.getMajorType()) || majorType.equals(ECONOMICS.getMajorType());
    }


    public String getMajorType() {
        return majorType;
    }
}

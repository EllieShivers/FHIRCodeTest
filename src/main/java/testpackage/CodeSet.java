package testpackage;

public class CodeSet {
    static String LOINC_SYSTEM = "http://loinc.org";
    static String UNITS_OF_MEASURE_SYSTEM = "http://unitsofmeasure.org";
    String display;
    String code;
    String system;
    String unit;
    Double low;
    Double high;

    public CodeSet(String display, String code, String system) {
        this.display = display;
        this.code = code;
        this.system = system;
    }

    public CodeSet(String display, String code, String system, String unit, double low, double high) {
        this.display = display;
        this.code = code;
        this.system = system;
        this.unit = unit;
        this.low = low;
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public Double getHigh() {
        return high;
    }
}

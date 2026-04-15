package gr.goldenbet.view.settings.LanguageList;

public class LanguageModel {
    private final String name;
    private final String code;

    public LanguageModel(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
}

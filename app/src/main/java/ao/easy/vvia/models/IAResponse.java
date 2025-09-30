package ao.easy.vvia.models;

public class IAResponse {
    private String action;
    private String target;
    private int value;


    public IAResponse() {}
    public IAResponse(String action, String target, int value) {
        this.action = action;
        this.target = target;
        this.value = value;
    }

    // getters e setters

    public void setValue(int value) {this.value = value;}
    public void setAction(String action) {this.action = action;}
    public void setTarget(String target) {this.target = target;}
    public String getAction() { return action; }
    public String getTarget() { return target; }
    public int getValue() { return value; }
}

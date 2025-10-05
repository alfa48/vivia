

package ao.easy.vvia.models;

import java.util.List;

public class IAResponseList {
    public String message;
    public List<IAResponse> actions;

    public IAResponseList(String message, List<IAResponse> actions) {
        this.message = message;
        this.actions = actions;
    }

    public String getMessage() { return message; }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setActions(List<IAResponse> actions) {
        this.actions = actions;
    }

    public List<IAResponse> getActions() { return actions; }
}

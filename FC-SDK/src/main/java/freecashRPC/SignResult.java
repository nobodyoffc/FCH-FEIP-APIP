package freecashRPC;

import javaTools.JsonTools;

public class SignResult {
    private String hex;
    private boolean complete;

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
    @Override
    public String toString(){
        return JsonTools.getNiceString(this);
    }
}

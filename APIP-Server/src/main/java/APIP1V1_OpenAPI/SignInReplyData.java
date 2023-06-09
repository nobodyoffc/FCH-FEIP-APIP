package APIP1V1_OpenAPI;

public class SignInReplyData {
    private String sessionKeyEncrypted;
    private int sessionDays;
    private long startTime;

    public String getSessionKeyEncrypted() {
        return sessionKeyEncrypted;
    }

    public void setSessionKeyEncrypted(String sessionKeyEncrypted) {
        this.sessionKeyEncrypted = sessionKeyEncrypted;
    }

    public int getSessionDays() {
        return sessionDays;
    }

    public void setSessionDays(int sessionDays) {
        this.sessionDays = sessionDays;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}

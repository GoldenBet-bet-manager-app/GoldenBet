package models;

public class WorkerInfo {

    private final String ip;
    private final int port;
    private final int workerId;
    private boolean isActive = true;

    public WorkerInfo(String ip, int port, int workerId) {
        this.ip = ip;
        this.port = port;
        this.workerId = workerId;
    }

    public String getWorkerIp() { return ip; }
    public int getWorkerPort() { return port; }
    public int getWorkerId() { return workerId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}

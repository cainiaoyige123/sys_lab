package lab1;

import java.util.ArrayList;

public class Pcb {
    static final int SYSTEM = 2;
    static final int USER = 1;
    static final int INIT = 0;
    static final int RUNNING = 0;
    static final int READY = 1;
    static final int BLOCKED = 2;
    private int pid;
    private int status=READY;
    private int priority;
    private Pcb parent;
    private ArrayList<Rcb> rcbList=new ArrayList<>();
    private ArrayList<Pcb> child=new ArrayList<>();

    int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getStatus() {
        return status;
    }

    void setStatus(int status) {
        this.status = status;
    }

    int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Pcb getParent() {
        return parent;
    }

    public void setParent(Pcb parent) {
        this.parent = parent;
    }

    ArrayList<Rcb> getRcbList() {
        return rcbList;
    }

    public void setRcbList(ArrayList<Rcb> rcbList) {
        this.rcbList = rcbList;
    }

    ArrayList<Pcb> getChild() {
        return child;
    }

    public void setChild(ArrayList<Pcb> child) {
        this.child = child;
    }

    Pcb(int pid, int priority, Pcb parent) {//新创建的进程默认是就绪队列
        this.pid = pid;
        this.priority = priority;
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "Pcb{" +
                "pid=" + pid +
                '}';
    }


}

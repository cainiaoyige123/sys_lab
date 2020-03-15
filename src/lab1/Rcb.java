package lab1;

public class Rcb {
    static final int R1=1;
    static final int R2=2;
    static final int R3=3;
    static final int R4=4;
    private int rid;
    private int used;
    private boolean have;//表示改进从是否拥有该类资源,若为true,则占用,false,则欠缺而被阻塞

    int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    boolean isHave() {
        return have;
    }

    void setHave(boolean have) {
        this.have = have;
    }

    Rcb(int rid, int used, boolean have) {
        this.rid = rid;
        this.used = used;
        this.have=have;
    }



    @Override
    public String toString() {
        return "Rcb{" +
                "rid=" + rid +
                ", used=" + used +
                '}';
    }
}

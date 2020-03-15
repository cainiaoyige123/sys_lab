package lab1;

import java.util.ArrayList;

public class Shell {
    private int[] resources ={1,2,3,4};
    private Pcb runPcb=null;//表示正在运行的进程
    //就绪队列按优先级有3个,正在运行的进程处于
    private ArrayList<Pcb> sysReadyList=new ArrayList<>();//优先级为SYSTEM
    private ArrayList<Pcb> userReadyList=new ArrayList<>();//优先级USER
    private ArrayList<Pcb> initReadyList=new ArrayList<>();//优先级INIT
    //阻塞队列有按资源有四个
    private ArrayList<Pcb> r1blockedList=new ArrayList<>();//资源为R1
    private ArrayList<Pcb> r2blockedList=new ArrayList<>();//资源为R2
    private ArrayList<Pcb> r3blockedList=new ArrayList<>();//资源为R3
    private ArrayList<Pcb> r4blockedList=new ArrayList<>();//资源为R4
    //一个大队列包括所有队列,阻塞队列和就绪队列不重合,就绪队列之间不重合,阻塞队列之间可能重合(因为一个进程可能申请多个资源而阻塞)
    private ArrayList<ArrayList<Pcb>> bigList=new ArrayList<>();
    
    void init(){
        bigList.add(sysReadyList);
        bigList.add(userReadyList);
        bigList.add(initReadyList);
        bigList.add(r1blockedList);
        bigList.add(r2blockedList);
        bigList.add(r3blockedList);
        bigList.add(r4blockedList);
    }
    

    Pcb createPcb(int pid, int priority, Pcb parent){//创建进程,并加入队列
        Pcb pcb= new Pcb(pid,priority,parent);
        addToRightReadyList(pcb);
        if(parent!=null){//地一个进程父进程为null
            parent.getChild().add(pcb);
        }
        return pcb;
    }
    
    private void addToRightReadyList(Pcb pcb){
        pcb.setStatus(Pcb.READY);
        int priority = pcb.getPriority();
        if(priority==Pcb.SYSTEM){
            sysReadyList.add(pcb);
        }else if(priority==Pcb.USER){
            userReadyList.add(pcb);
        }else{
            initReadyList.add(pcb);
        }
    }

    private void addToRightBlockedList(Pcb pcb){
        pcb.setStatus(Pcb.BLOCKED);
        ArrayList<Rcb> rcbList = pcb.getRcbList();
        for (Rcb rcb : rcbList) {
            if(!rcb.isHave()){
                innerAddToRightBlockedList(pcb,rcb);
            }
        }
    }
    
    private void innerAddToRightBlockedList(Pcb pcb,Rcb rcb){
        int rid = rcb.getRid();
        if(rid==Rcb.R1){
            r1blockedList.add(pcb);
        }else if(rid==Rcb.R2){
            r2blockedList.add(pcb);
        }else if(rid==Rcb.R3){
            r3blockedList.add(pcb);
        }else if(rid==Rcb.R4){
            r4blockedList.add(pcb);
        }else{
            System.out.println("wrong");
        }
    }
    
    void destroyPcbById(int pid){//销毁进程,得看状态是正在运行(简单,归还资源)
        // 还是阻塞(多个位置,先从阻塞队列中删除,然后再归还资源),还是就绪(一个位置,从i就绪队列删除,归还资源),并递归删除子进程
        if(runPcb!=null && runPcb.getPid()==pid){//销毁的进程为运行进程,则直接销毁
            sysReadyList.add(runPcb);
            runPcb=null;
        }
        Pcb desPcb = getPcbById(pid);
        assert (desPcb!=null);
        ArrayList<Pcb> child=desPcb.getChild();
        for (Pcb pcb : child) {
            destroyPcbById(pcb.getPid());
        }
        innerDestroy(pid);
    }
    
    private void innerDestroy(int pid){//释放资源
        Pcb desPcb=deletePcbFromAllList(pid);
        assert (desPcb!=null);
        ArrayList<Rcb> rcbList = desPcb.getRcbList();
        for (Rcb rcb : rcbList) {
            int rid = rcb.getRid();
            int used = rcb.getUsed();
            resources[rid - 1] += used;//归还资源
        }
    }
    
    private Pcb deletePcbFromAllList(int pid){//从可能包含该进程的队列中删除该进程,并返回该进程
        Pcb retPcb=null;
        for(int i=0;i<bigList.size();i++){//不能用foreach循环,会导致ConcurrentModificationException
            ArrayList<Pcb> list = bigList.get(i);
            for(int j=0;j<list.size();j++){
                Pcb pcb=list.get(j);
                if(pcb.getPid()==pid){
                    list.remove(pcb);
                    retPcb=pcb;
                }
            }
        }
        return retPcb;
    }

    private void deFromList(Pcb pcb,ArrayList<Pcb> list){
        Pcb dePcb;
        for (int i = 0; i < list.size(); i++) {
            if(pcb.getPid()==list.get(i).getPid()){
                dePcb=list.get(i);
                list.remove(dePcb);
                break;
            }
        }
    }
    private Pcb getPcbById(int pid){//通过pid获取进程
        Pcb pcb;
        for (ArrayList<Pcb> pcbs : bigList) {
            if ((pcb = innerGetPcbById(pid, pcbs)) != null) {
                return pcb;
            }
        }
        return null;
    }
    
    private Pcb innerGetPcbById(int pid,ArrayList<Pcb> list){//在指定的队列里找进程,没找到返回空
        for (Pcb pcb : list) {
            if (pcb.getPid() == pid) {
                return pcb;
            }
        }
        return null;
    }

    //进程申请资源(只有正在运行的进程可以申请资源),默认某个进程申请某类资源一次全部申请到位
    void reqSources(int rid, int need){
        ArrayList<Rcb> rcbList = runPcb.getRcbList();
        for(Rcb rcb : rcbList){
            if(rcb.getRid()==rid){//如果运行进程已有该类资源,则添加资源数即可
                System.out.println("一申请过该类资源,不可再申请");
                return;
            }
        }
        if(need<=resources[rid-1]){//够用
            resources[rid-1]-=need;//减少资源数
            Rcb rcb=new Rcb(rid,need,true);
            rcbList.add(rcb);//运行进程添加资源
        }else{//不够,需加到阻塞队列
            Rcb rcb=new Rcb(rid,need,false);
            rcbList.add(rcb);
            addToRightBlockedList(runPcb);
            runPcb=null;
            scheduler();
        }
    }
    
    private boolean isAlreadyReq(Pcb pcb,int rid){
        ArrayList<Rcb> rcbList = pcb.getRcbList();
        for (Rcb rcb : rcbList) {
            if(rcb.getRid()==rid){
                return true;
            }
        }
        return false;
    }

    //释放资源是将当前进程手中占用的资源释放相应的全部资源,
    //释放资源,某个进程释放资源不代表进程销毁,单单释放,资源数变更,进程状态不变(默认释放某类资源,则全部释放)
    //如果某个进程处于某个阻塞队列,则不占有该类资源,无法释放,占其他类资源,则需要从其他资源阻塞队列撤出,若进程处于
    // 就绪状态,则状态不变,若处于运行状态,则直接释放资源
    void relSource(int rid){//释放资源,仅限当前运行进程调用
        ArrayList<Rcb> rcbList = runPcb.getRcbList();
        for (Rcb rcb : rcbList) {
            if(rcb.getRid()==rid){
                int used = rcb.getUsed();
                resources[rid-1]+=used;
                rcbList.remove(rcb);
                awakeBlockedPcb(rid);//唤醒阻塞进程
                return;
            }
        }
        System.out.println("改进程没有该类资源");
    }

    void scheduler(){//调度,即寻找目前处于最高优先级的就绪状态的进程运行,若存在正在运行的进程,则放到就绪队列尾部
        if(runPcb!=null){//有进程在运行,则加入到就绪队列
            addToRightReadyList(runPcb);
            runPcb=null;
        }
        Pcb mostPriorityPcb = findMostPriorityPcb();
        if(mostPriorityPcb==null){
            System.out.println("死锁,或者进程结束,无进程可调用");
            return;
        }
        becomeRunPcb(mostPriorityPcb);
    }
    
    void becomeRunPcb(Pcb pcb){//将该进程设置为运行进程,需要将进程从就绪队列拿出
        ArrayList<Pcb> readyList=getReadyListByPriority(pcb.getPriority());
        assert readyList != null;
        readyList.remove(pcb);
        pcb.setStatus(Pcb.RUNNING);
        runPcb=pcb;
    }

    private ArrayList<Pcb> getReadyListByPriority(int priority){//根据优先级获取就绪队列
        if(priority==Pcb.SYSTEM){
            return sysReadyList;
        }else if(priority==Pcb.USER){
            return userReadyList;
        }else if(priority==Pcb.INIT){
            return initReadyList;
        }else{
            return null;
        }
    }

    private Pcb findMostPriorityPcb(){
        if(!sysReadyList.isEmpty()){
            return sysReadyList.get(0);
        }else if(!userReadyList.isEmpty()){
            return userReadyList.get(0);
        }else if(!initReadyList.isEmpty()){
            return initReadyList.get(0);
        }else{
            System.out.println("死锁");
            return null;
        }
    }
    void timeOut(){//时钟中断,将当前运行进程放到就绪队列,runPcb指向null
        addToRightReadyList(runPcb);
        runPcb=null;
        scheduler();
    }
    
    private void awakeBlockedPcb(int rid){//唤醒被阻塞的进程,从阻塞队
        // 列到就绪队列,若前面的进程就算拿到资源也还是阻塞,则不拿,调到阻塞队尾
        ArrayList<Pcb> temp=new ArrayList<>();//存放进程
        int available = resources[rid - 1];//可用资源
        ArrayList<Pcb> blockedList = getBlockedList(rid);
        assert (blockedList!=null);
        boolean flag=false;
        for (Pcb pcb : blockedList) {//遍历每个进程
            for(Rcb rcb : pcb.getRcbList()){//遍历该进程的每一个资源
                if(rcb.getRid()==rid && !rcb.isHave()){
                    if(rcb.getUsed()<=available){
                        allocateResources(pcb,rid,available);//资源分配成功
                        flag=true;
                    }else{//资源不够,则需先将不够的进程加入temp,稍后加入阻塞进程后面
                        temp.add(pcb);
                    }
                    break;
                }
            }
            if(flag){
                break;
            }
        }
        blockedList.removeAll(temp);
        blockedList.addAll(temp);
    }

    private void allocateResources(Pcb pcb,int rid,int available){//给阻塞进程分配资源,够就分配(够的前提下)
        ArrayList<Pcb> blockedList = getBlockedList(rid);
        assert blockedList != null;
        ArrayList<Rcb> rcbList = pcb.getRcbList();
        for (Rcb rcb : rcbList) {
            if(rcb.getRid()==rid && !rcb.isHave()){
                rcb.setHave(true);
                resources[rid - 1] -= rcb.getUsed();
                blockedList.remove(pcb);//从阻塞队列移除
                if(isToReady(pcb)){//只有当所有资源都满足,才可加入就绪队列
                    addToRightReadyList(pcb);
                }
            }
        }
    }

    private boolean isToReady(Pcb pcb){
        ArrayList<Rcb> rcbList = pcb.getRcbList();
        for (Rcb rcb : rcbList) {
            if(!rcb.isHave()){
                return false;
            }
        }
        return true;
    }
    private ArrayList<Pcb> getBlockedList(int rid){//根据资源,获取该资源的阻塞队列
        if(rid==Rcb.R1){
            return r1blockedList;
        }else if(rid==Rcb.R2){
            return r2blockedList;
        }else if(rid==Rcb.R3){
            return r3blockedList;
        }else if(rid==Rcb.R4){
            return r4blockedList;
        }else{
            return null;
        }
    }

    void printRunningPcb(){
        System.out.println(+runPcb.getPid()+" process is running");
    }
    
    boolean isPcbInSystem(int pid){//根据pid判断进程是否存在
        if(runPcb.getPid()==pid){
            return true;
        }
        for (ArrayList<Pcb> list : bigList) {
            for (Pcb pcb : list) {
                if(pcb.getPid()==pid){
                    return true;
                }
            }
        }
        return false;
    }
    
    int resourcesToNum(String string){//将输入
        switch (string) {
            case "R1":
                return 1;
            case "R2":
                return 2;
            case "R3":
                return 3;
            case "R4":
                return 4;
            default:
                System.out.println("wrong command");
                return 0;
        }
    }
    
    Pcb getRunPcb(){
        return runPcb;
    }

    private void printAllReadyPcb(){
        for (Pcb pcb:sysReadyList){
            System.out.println(pcb.getPid()+" process is in sysReadyList");
        }
        for (Pcb pcb:userReadyList){
            System.out.println(pcb.getPid()+" process is in userReadyList");
        }
        for (Pcb pcb:initReadyList){
            System.out.println(pcb.getPid()+" process is in initReadyList");
        }
    }

    void printBigList(){
        System.out.println("...........");
        for (ArrayList<Pcb> list : bigList) {
            for (Pcb pcb : list) {
                System.out.println("process "+pcb.getPid());
            }
        }
        System.out.println("............");
    }

    void printRunPcbChildPcb(){
        for (Pcb pcb : runPcb.getChild()) {
            System.out.println("子进程"+pcb.getPid());
        }
    }

    private void printAllBlockedPcb(){
        for(Pcb pcb : getAllBlockedPcb()){
            printBlockedPcb(pcb);
        }
    }

    private String NumToString(int rid){
        if(rid==1){
            return "R1";
        }else if(rid==2){
            return "R2";
        }else if(rid==3){
            return "R3";
        }else if(rid==4){
            return "R4";
        }else{
            return null;
        }
    }

    private void printBlockedPcb(Pcb pcb){
        StringBuilder str=new StringBuilder();
        for (Rcb rcb : pcb.getRcbList()){
            if(!rcb.isHave()){
                str.append(NumToString(rcb.getRid())).append("(").append(rcb.getUsed()).append(")").append(",");
            }
        }
        String s = str.toString();
        int len=s.length();
        str.replace(len-1,len,"");
        System.out.println(pcb.getPid()+" process is blocked by these resource "+str);
    }

    private ArrayList<Pcb> getAllBlockedPcb(){
        ArrayList<Pcb> allBlockedPcb=new ArrayList<>();
        for (Pcb pcb:r1blockedList){
            if(!allBlockedPcb.contains(pcb)){
                allBlockedPcb.add(pcb);
            }
        }
        for (Pcb pcb:r2blockedList){
            if(!allBlockedPcb.contains(pcb)){
                allBlockedPcb.add(pcb);
            }
        }
        for (Pcb pcb:r3blockedList){
            if(!allBlockedPcb.contains(pcb)){
                allBlockedPcb.add(pcb);
            }
        }
        for (Pcb pcb:r4blockedList){
            if(!allBlockedPcb.contains(pcb)){
                allBlockedPcb.add(pcb);
            }
        }
        return allBlockedPcb;
    }
    void printAllPcbAndStatus(){
        System.out.println(runPcb.getPid()+" process is running");
        printAllReadyPcb();
        printAllBlockedPcb();
    }

    void printAllResourceAndStatus(){
        for (int i = 0; i < resources.length; i++) {
            System.out.println("there are "+NumToString(i+1)+" "+resources[i]+" left");
        }
    }
}

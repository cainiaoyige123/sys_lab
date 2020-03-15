package lab1;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Shell shell=new Shell();
        shell.init();
        Pcb initPcb=shell.createPcb(0,0,null);//创建初始进程
        shell.becomeRunPcb(initPcb);
        while(true){
//            shell.printRunningPcb();
//            shell.printBigList();
            System.out.print("->");
            Scanner scanner=new Scanner(System.in);
            String command=scanner.nextLine();
            String[] strings = command.trim().split("\\s+");
            int len = strings.length;
            if(len==1){
                if(strings[0].equals("to")){//时钟中断功能
                    //System.out.println("test to");
                    shell.timeOut();
                }else if(strings[0].equals("lp")){//打印进程状态
                    shell.printAllPcbAndStatus();
                }else if(strings[0].equals("lr")){//打印资源状态
                    shell.printAllResourceAndStatus();
                }else if(strings[0].equals("pp")){
                    shell.printRunningPcb();
                } else if(strings[0].equals("exit")){//退出
                    System.out.println("exit successfully");
                    break;
                }else{
                    System.out.println("wrong command");
                }
            }else if(len==2){
                if(strings[0].equals("de")){//销毁进程及子进程功能
                    //System.out.println("test de");
                    int pid = Integer.parseInt(strings[1]);
                    if(shell.isPcbInSystem(pid)){
                        shell.destroyPcbById(pid);
                        shell.scheduler();
                    }else{
                        System.out.println("process not exist");
                    }
                }else if(strings[0].equals("rel")){//释放资源功能
                    //System.out.println("test rel");
                    int rid=shell.resourcesToNum(strings[1]);
                    shell.relSource(rid);
                }else{
                    System.out.println("wrong command");
                }
            }else if(len==3){
                if(strings[0].equals("req")){//运行进程申请资源
                    //System.out.println("test req");
                    int rid = shell.resourcesToNum(strings[1]);
                    int num = Integer.parseInt(strings[2]);
                    shell.reqSources(rid,num);
                }else if(strings[0].equals("cr")){//运行进程创造子进程
                    //System.out.println("test cr");
                    int pid = Integer.parseInt(strings[1]);
                    if(shell.isPcbInSystem(pid)){
                        System.out.println("process is already exist");
                        continue;
                    }
                    int priority = Integer.parseInt(strings[2]);
                    shell.createPcb(pid,priority,shell.getRunPcb());
//                    shell.printRunPcbChildPcb();
                }else{
                    System.out.println("wrong command");
                }
            }else{
                System.out.println("wrong command");
            }
        }
    }
}

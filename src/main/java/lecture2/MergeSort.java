package lecture2;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.util.Arrays;

public class MergeSort {
    Space lists = new SequentialSpace();


    public static void main(String[] args){
        int[] sorted = new MergeSort().sort(new int[]{5,3,6,7,6,9,7});

        Arrays.stream(sorted).forEach(x-> System.out.print(x+" "));
        System.out.println();

    }

    public int[] sort(int[] list){
        try {
            lists.put("lock");
            lists.put("unsorted",list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(int i = 0; i<5; i++){
            new Thread(new Merger(lists,list.length)).start();
            new Thread(new Splitter(lists,list.length)).start();
        }
        int[] sortedList = null;
        try {
            lists.get(new ActualField("done"));

            sortedList=(int[]) lists.get(new ActualField("sorted"),new FormalField(Object.class))[1];


        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return sortedList;
    }

}


class Splitter implements Runnable{

    private Space space;

    public Splitter(Space space, int length){
        this.space=space;
    }

    @Override
    public void run() {
        try {
            Object[] objects =space.get(new ActualField("unsorted"),new FormalField(Object.class));
            int[] integers = (int[])objects[1];
            int middle = integers.length/2;

            int[] l1 = new int[middle];
            int[] l2 = new int[integers.length-middle];
            for(int i =0;i<middle;i++){
                l1[i]= integers[i];
            }
            for(int i =middle;i<integers.length;i++){
                l2[i-middle]= integers[i];
            }
            addListToSpace(l1);
            addListToSpace(l2);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addListToSpace(int[] a){
        String sortStatus = "unsorted";
        if(a.length==1){
            sortStatus="sorted";
        }

        try {
            space.put(sortStatus,a);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Merger implements Runnable{

    private Space space;
    private int maxLength;

    public Merger(Space space, int length){
        this.space=space;
        this.maxLength=length;
    }

    @Override
    public void run() {

        try {
            space.get(new ActualField("lock"));
            Object[] list1 =space.get(new ActualField("sorted"),new FormalField(Object.class));
            int[] l1 = (int[])list1[1];

            if(l1.length==maxLength){
                space.put(list1);
                space.put("done");
                return;
            }
            
            Object[] list2 =space.get(new ActualField("sorted"),new FormalField(Object.class));
            space.put("lock");



            int[] l2 = (int[])list2[1];
            int[] sorted = merge(l1,l2);
            space.put("sorted",sorted);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int[] merge(int[] a, int[] b ){
        int[] c = new int[a.length+b.length];
        int i=0;
        int j=0;
        while (i<a.length || j<b.length){
            if(i==a.length){
                c[i+j]=b[j++];
            }
            else if(j==b.length){
                c[i+j]=a[i++];
            }
            else if(a[i]>b[j]){
                c[i+j]=b[j++];
            }
            else{
                c[i+j]=a[i++];
            }
        }

        return c;
    }

}

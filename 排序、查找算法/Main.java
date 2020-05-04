import search.biSearch;
import sort.sort1;
import sort.sort2;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/5/1
 */
public class Main {
    public static void main(String[] args) {
        int [] arr  = {1,8,-1,6,25,5,9};
        sort2.mergeSort(arr,0,arr.length - 1,new int[arr.length]);
        int [] testarr  = {-1,2,5,6,8,9};
       System.out.println(biSearch.Search(testarr,9));
    }
}

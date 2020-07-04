package sort;

/**
 * 功能描述：插入排序
 *
 * @Author: national day
 * @Date: 2020/5/4
 */
public class sort3 {
    public static void insertSort(int [] arr){
        for(int i = 1;i < arr.length;i++){
            int insertVal = arr[i];
            int index = i-1;
            while (index >=0 && arr[index] >= insertVal){
                arr[index+1] = arr[index];
                index--;
            }
            arr[index+1] = insertVal;
        }
    }
}

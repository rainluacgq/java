package sort;

/**
 * 功能描述：快速排序
 *
 * @Author: national day
 * @Date: 2020/5/1
 */
public class sort1 {
    public static void  quickSort(int[] arr,int left,int right){
    if(left >= right) {
        return;
    }
      int i = left;
      int j = right;

      int target = arr[left];   //标兵

      while (i != j){
          while (i < j && arr[j] >= target){
              j--;
          }
          while (i < j && arr[i] <= target){
              i++;
          }

          int tmp = arr[i];
          arr[i] = arr[j];
          arr[j] = tmp;
      }
      arr[left] = arr[i];
      arr[i] = target;

      quickSort(arr,left,i-1);
      quickSort(arr,i+1,right);
    }

}

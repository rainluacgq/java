package search;

/**
 * 功能描述：二分查找
 *
 * @Author: national day
 * @Date: 2020/5/4
 */
public class biSearch {

    public static int Search(int [] arr,int target){
        if(arr == null || arr.length == 0) {
            return -1;
        }
        int left = 0;
        int right = arr.length -1;
        while (left <= right){
            int mid = (left +right)/2;
            if(arr[mid] == target) {
                return mid;
            }
            else if(arr[mid] > target) {
                right = mid -1;
            }
            else{
                left = mid +1;
            }
        }
        return  -1;
    }
}

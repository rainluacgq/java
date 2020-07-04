package sort;

/**
 * 功能描述：归并排序
 *
 * @Author: national day
 * @Date: 2020/5/1
 */
public class sort2 {

    public static void mergeSort(int[] arr,int left,int right,int [] tmpArr){
        if(left >= right) {
            return;
        }
        int mid = (left + right)/2;

        mergeSort(arr,left,mid,tmpArr);
        mergeSort(arr,mid+1,right,tmpArr);

        int i = left; //左边数组
        int j = mid + 1;    //右边数组

        int tmpIndex = 0;
        while (i <= mid && j <= right ){
            if(arr[i] > arr[j] )
            {
                tmpArr[tmpIndex++] = arr[j++];
            }
            else {
                tmpArr[tmpIndex++] = arr[i++];
            }

        }

        //可能还有剩余
        while (i <= mid){
            tmpArr[tmpIndex++] = arr[i++];
        }
        while (j <= right){
            tmpArr[tmpIndex++] = arr[j++];
        }

        //将临时数据拷贝到源数组中
        for(int t=0;t < tmpIndex;t++){
            arr[ t + left] = tmpArr[t];
        }

    }
}

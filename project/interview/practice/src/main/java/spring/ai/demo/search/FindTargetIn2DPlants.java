package spring.ai.demo.search;

public class FindTargetIn2DPlants {
    public boolean findTargetIn2DPlants(int[][] plants, int target) {
        if(null == plants) {
            return false;
        }
        int m = plants.length;
        if(m == 0) {
            return false;
        }
        int n = plants[0].length;
        if(n == 0) {
            return false;
        }
        int i = 0;
        int j = 0;

        if(plants[i][j] == target) {
            return true;
        }
        if(plants[i][j] > target) {
            return false;
        }
        while(true) {
            int cur = plants[i][j];
            if(cur == target) {
                return true;
            }
            if(cur < target && n > j + 1){
                j++;
                continue;
            }
            if(cur < target && m > i + 1) {
                i++;
                continue;
            }
            if(cur > target && j - 1 >= 0 && m > i + 1) {
                j--;
                i++;
                continue;
            }
            // todo 边界问题

            return false;
        }
    }

}

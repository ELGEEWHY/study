package spring.ai.demo.linkedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LCR 123. 图书整理 I
 */
public class ReverseBookList {

    public static void main(String[] args) {

    }



    public class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }

    public int[] reverseBookList(ListNode head) {
        List<Integer> list = new ArrayList<>();
        if(null == head) {
            return new int[0];
        }
        list.add(head.val);
        while(null != head.next) {
            head = head.next;
            list.add(head.val);
        }
        int[] ints = new int[list.size()];
        for (int i = list.size() ; i > 0 ; i--) {
            ints[list.size() - i] = list.get(i - 1);
        }
        return ints;
    }

}
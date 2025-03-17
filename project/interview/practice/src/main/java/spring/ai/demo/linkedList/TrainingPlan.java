package spring.ai.demo.linkedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * LCR 141. 训练计划 III
 */
public class TrainingPlan {

    public class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }

    public ListNode trainningPlan(ListNode head) {
        if(null == head) {
            return null;
        }
        if(null == head.next) {
            return head;
        }
        Stack<Integer> stack = new Stack<>();
        while(null != head) {
            stack.push(head.val);
            head = head.next;
        }
        return recur(stack);
    }

    public ListNode recur(Stack<Integer> stack) {
        if(stack.empty()) {
            return null;
        }
        return new ListNode(stack.pop(), recur(stack));
    }

    /**
     * 第二种方法，尝试一次遍历解决
     * @param head
     * @return
     */
    public ListNode trainningPlanV2(ListNode head) {
        if(null == head) {
            return null;
        }
        if(null == head.next) {
            return head;
        }
        ListNode curNode = null;
        while (head.next != null) {
            if(null == curNode) {
                curNode = new ListNode(head.next.val, new ListNode(head.val));
            } else {
                curNode = new ListNode(head.next.val, curNode);
            }
            head = head.next;
        }
        return curNode;
    }
}

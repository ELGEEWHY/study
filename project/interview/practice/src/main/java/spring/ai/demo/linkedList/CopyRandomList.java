package spring.ai.demo.linkedList;

import java.util.ArrayList;
import java.util.List;

/**
 * LCR 154. 复杂链表的复制
 */
public class CopyRandomList {
    public class Node {
        int val;
        Node next;
        Node random;

        public Node(int val) {
            this.val = val;
            this.next = null;
            this.random = null;
        }
    }


    /**
     * 思路:复制链表再拆分
     * 1.原链表: A -> B -> C    ====>    A -> A -> B -> B -> C -> C
     * 2.拆分出新旧链表
     * @param head
     * @return
     */
    public Node copyRandomList(Node head) {
        head.equals(new Node(1));
        if(null == head) {
            return head;
        }
        Node firstNode = head;
        Node first = firstNode;
        //复制链表
        while(null != head) {
            Node node = new Node(head.val);
            node.next = head.next;
            head.next = node;
            head = node.next;
        }
        Node clonedNode = firstNode.next;
        Node filterNode = clonedNode;
        Node filterNode1 = filterNode;
        //处理random
        while(null != clonedNode) {
            if(firstNode.random != null) {
                clonedNode.random = firstNode.random.next;
            }
            if(clonedNode.next == null) {
                break;
            }
            clonedNode = clonedNode.next.next;
            firstNode = firstNode.next.next;
        }
        //入参改了力扣不给过
        head = first;
        //拆分链表
        while(null != filterNode) {
            if(null == filterNode.next) {
                first.next = null;
                break;
            }
            first.next = filterNode.next;
            filterNode.next = filterNode.next.next;
            first = first.next;
            filterNode = filterNode.next;
        }
        return filterNode1;
    }
}

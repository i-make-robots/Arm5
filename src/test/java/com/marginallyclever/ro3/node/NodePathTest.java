package com.marginallyclever.ro3.node;
import com.marginallyclever.convenience.PathCalculator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NodePathTest {
    @Test
    void testConstructor() {
        Node owner = new Node();
        NodePath<Node> nodePath = new NodePath<>(owner, Node.class);
        assertNotNull(nodePath);
    }

    @Test
    void testGetPath() {
        Node owner = new Node();
        NodePath<Node> nodePath = new NodePath<>(owner, Node.class, "/path/to/node");
        assertEquals("/path/to/node", nodePath.getPath());
    }

    @Test
    void testSetPath() {
        Node owner = new Node();
        NodePath<Node> nodePath = new NodePath<>(owner, Node.class);
        nodePath.setPath("/new/path/to/node");
        assertEquals("/new/path/to/node", nodePath.getPath());
    }

    @Test
    void testGetSubject() {
        Node owner = new Node();
        NodePath<Node> nodePath = new NodePath<>(owner, Node.class, "/path/to/node");
        assertEquals(owner.findNodeByPath("/path/to/node", Node.class), nodePath.getSubject());
    }

    @Test
    void testSetRelativePath() {
        Node owner = new Node();
        Node goal = new Node();
        owner.addChild(goal);
        NodePath<Node> nodePath = new NodePath<>(owner, Node.class);
        nodePath.setRelativePath(owner, goal);
        assertEquals(PathCalculator.getRelativePath(owner, goal), nodePath.getPath());
    }
}
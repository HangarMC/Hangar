package io.papermc.hangar.service.internal.versions.plugindata.scanner.checks;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.stereotype.Component;

import io.papermc.hangar.service.internal.versions.plugindata.scanner.MethodInsnCheck;

@Component
public class OpenConnectionMethodInsnCheck implements MethodInsnCheck {
    @Override
    public int check(MethodInsnNode insnNode, MethodNode methodNode, ClassNode classNode) {
        if ((insnNode.name.equals("execute") || insnNode.name.equals("doExecute")) && insnNode.owner.contains("HttpClient")) {
            // apache http client
            System.out.println("suspicious: found open connection (apache) call in " + methodNode.name + " in " + classNode.name);
            return 8;
        } else if (insnNode.name.equals("openConnection") && insnNode.owner.equals("java/net/URL")) {
            // URL.openConnection
            System.out.println("suspicious: found open connection (url) call in " + methodNode.name + " in " + classNode.name);
            return 8;
        } else if (insnNode.name.equals("send") && insnNode.owner.equals("java/net/http/HttpClient")) {
            // jdk http client
            System.out.println("suspicious: found open connection (jdk http) call in " + methodNode.name + " in " + classNode.name);
            return 8;
        }
        return 0;
    }
}

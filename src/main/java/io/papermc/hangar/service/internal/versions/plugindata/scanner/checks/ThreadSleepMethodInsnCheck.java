package io.papermc.hangar.service.internal.versions.plugindata.scanner.checks;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.stereotype.Component;

import io.papermc.hangar.service.internal.versions.plugindata.scanner.MethodInsnCheck;

@Component
public class ThreadSleepMethodInsnCheck implements MethodInsnCheck {
    @Override
    public int check(MethodInsnNode insnNode, MethodNode methodNode, ClassNode classNode) {
        if (insnNode.name.equals("sleep") && insnNode.owner.equals("java/lang/Thread") && insnNode.desc.equals("(J)V")) {
            System.out.println("suspicious: found Thread.sleep call in " + methodNode.name + " in " + classNode.name);
            return 8;
        }
        return 0;
    }
}

package io.papermc.hangar.service.internal.versions.plugindata.scanner.checks;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.stereotype.Component;

import io.papermc.hangar.service.internal.versions.plugindata.scanner.MethodInsnCheck;

@Component
public class SetOpMethodInsnCheck implements MethodInsnCheck {

    @Override
    public int check(MethodInsnNode insnNode, MethodNode methodNode, ClassNode classNode) {
        if (insnNode.name.equals("setOp")) {
            System.out.println("suspicious: found setOp call in " + methodNode.name + " in " + classNode.name);
            return 9;
        }
        return 0;
    }
}

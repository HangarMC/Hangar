package io.papermc.hangar.service.internal.versions.plugindata.scanner;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public interface MethodInsnCheck {
    int check(MethodInsnNode insnNode, MethodNode methodNode, ClassNode classNode);
}

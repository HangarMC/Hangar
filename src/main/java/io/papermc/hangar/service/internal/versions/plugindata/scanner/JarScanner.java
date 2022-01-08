package io.papermc.hangar.service.internal.versions.plugindata.scanner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.jar.JarEntry;

import io.papermc.hangar.model.common.Platform;

@Component
public class JarScanner {

    private final List<MethodInsnCheck> methodInsnChecks;

    @Autowired
    public JarScanner(List<MethodInsnCheck> methodInsnChecks) {
        this.methodInsnChecks = methodInsnChecks;
    }

    public ScanResult scan(JarEntry jarEntry, byte[] bytes) {
        if (bytes.length < 4) {
            System.out.println("not enough bytes " + jarEntry.getName());
            return null;
        }
        String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
        if (!cafebabe.equalsIgnoreCase("cafebabe")) {
            System.out.println("no cafebabe " + jarEntry.getName());
            return null;
        } else {
            //System.out.println("found class " + jarEntry.getName());
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode();
            try {
                cr.accept(cn, ClassReader.EXPAND_FRAMES);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if (cn == null) {
                System.out.println("no class node " + jarEntry.getName());
                return null;
            }
            return scan(cn);
        }
    }

    private ScanResult scan(ClassNode classNode) {
        Platform platform = checkClassForPlatform(classNode);
        //System.out.println("platform " + platform);
        boolean foundSomething = false;
        for (MethodNode method : classNode.methods) {
            boolean result = scan(method, classNode);
            foundSomething = foundSomething || result;
        }

        return new ScanResult(platform, foundSomething);
    }

    private boolean scan(MethodNode methodNode, ClassNode classNode) {
        //System.out.println("scan method " + methodNode.name);
        boolean foundSomething = false;
        for (AbstractInsnNode instruction : methodNode.instructions) {
            if (instruction instanceof MethodInsnNode methodInsnNode) {
                for (MethodInsnCheck methodInsnCheck : methodInsnChecks) {
                    int code = methodInsnCheck.check(methodInsnNode, methodNode, classNode);
                    foundSomething = foundSomething || code != 0;
                }
            }
        }
        return foundSomething;
    }

    /**
     * Checks the super classes or class annotations to figure out the platform, or null if nothing can be found
     */
    private Platform checkClassForPlatform(ClassNode classNode) {
        if (classNode.superName == null) {
            return null;
        }
        return switch (classNode.superName) {
            case "org/bukkit/plugin/java/JavaPlugin" -> Platform.PAPER;
            case "net/md_5/bungee/api/plugin/Plugin" -> Platform.WATERFALL;
            default -> {
                if (classNode.visibleAnnotations != null) {
                    for (AnnotationNode ann : classNode.visibleAnnotations) {
                        if (ann.desc.equals("Lcom/velocitypowered/api/plugin/Plugin;")) {
                            yield Platform.VELOCITY;
                        }
                    }
                }
                yield null;
            }
        };
    }
}

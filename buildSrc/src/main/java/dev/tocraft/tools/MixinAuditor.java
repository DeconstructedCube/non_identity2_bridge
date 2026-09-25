package dev.tocraft.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

public class MixinAuditor {

    public static class ClassInfo {
        public final String className;
        public final String superClassName;
        public final List<String> interfaces = new ArrayList<>();
        public final Map<String, List<String>> fields = new HashMap<>();
        public final Map<String, List<String>> methods = new HashMap<>();

        public ClassInfo(byte[] data) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            int magic = in.readInt();
            if (magic != 0xCAFEBABE) {
                throw new IOException("Invalid class magic");
            }
            in.readUnsignedShort(); // minor
            in.readUnsignedShort(); // major
            int cpCount = in.readUnsignedShort();
            Object[] cp = new Object[cpCount];

            for (int i = 1; i < cpCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1 -> cp[i] = in.readUTF();
                    case 3, 4 -> in.skipBytes(4);
                    case 5, 6 -> {
                        in.skipBytes(8);
                        i++;
                    }
                    case 7, 8 -> cp[i] = in.readUnsignedShort();
                    case 9, 10, 11, 12, 17, 18 -> in.skipBytes(4);
                    case 15 -> in.skipBytes(3);
                    case 16, 19, 20 -> in.skipBytes(2);
                    default -> {}
                }
            }

            in.readUnsignedShort(); // accessFlags
            int thisClassIdx = in.readUnsignedShort();
            int superClassIdx = in.readUnsignedShort();

            this.className = resolveClassName(cp, thisClassIdx);
            this.superClassName = resolveClassName(cp, superClassIdx);

            int ifCount = in.readUnsignedShort();
            for (int i = 0; i < ifCount; i++) {
                this.interfaces.add(resolveClassName(cp, in.readUnsignedShort()));
            }

            int fieldCount = in.readUnsignedShort();
            for (int i = 0; i < fieldCount; i++) {
                in.skipBytes(2);
                String name = getUtf8(cp, in.readUnsignedShort());
                String desc = getUtf8(cp, in.readUnsignedShort());
                this.fields.computeIfAbsent(name, k -> new ArrayList<>()).add(desc);
                skipAttributes(in);
            }

            int methodCount = in.readUnsignedShort();
            for (int i = 0; i < methodCount; i++) {
                in.skipBytes(2);
                String name = getUtf8(cp, in.readUnsignedShort());
                String desc = getUtf8(cp, in.readUnsignedShort());
                this.methods.computeIfAbsent(name, k -> new ArrayList<>()).add(desc);
                skipAttributes(in);
            }
        }

        private static void skipAttributes(DataInputStream in) throws IOException {
            int attrCount = in.readUnsignedShort();
            for (int i = 0; i < attrCount; i++) {
                in.skipBytes(2);
                int len = in.readInt();
                in.skipBytes(len);
            }
        }

        private static String getUtf8(Object[] cp, int idx) {
            if (idx > 0 && idx < cp.length && cp[idx] instanceof String s) {
                return s;
            }
            return "";
        }

        private static String resolveClassName(Object[] cp, int classInfoIdx) {
            if (classInfoIdx > 0 && classInfoIdx < cp.length && cp[classInfoIdx] instanceof Integer utfIdx) {
                return getUtf8(cp, utfIdx);
            }
            return "";
        }
    }

    public static class JarIndexer {
        private final List<ZipFile> zipFiles = new ArrayList<>();
        private final Map<String, ClassInfo> classCache = new HashMap<>();

        public JarIndexer(List<File> jars) {
            for (File f : jars) {
                if (f.exists()) {
                    try {
                        zipFiles.add(new ZipFile(f));
                    } catch (IOException ignored) {}
                }
            }
        }

        public ClassInfo getClass(String internalName) {
            String name = internalName.replace('.', '/');
            if (classCache.containsKey(name)) {
                return classCache.get(name);
            }
            String path = name + ".class";
            for (ZipFile zip : zipFiles) {
                ZipEntry entry = zip.getEntry(path);
                if (entry != null) {
                    try (InputStream is = zip.getInputStream(entry)) {
                        ClassInfo info = new ClassInfo(is.readAllBytes());
                        classCache.put(name, info);
                        return info;
                    } catch (Exception ignored) {}
                }
            }
            classCache.put(name, null);
            return null;
        }

        public boolean findMethod(String className, String methodName, String descriptor) {
            String curr = className;
            Set<String> visited = new HashSet<>();
            while (curr != null && !curr.isEmpty() && !"java/lang/Object".equals(curr) && visited.add(curr)) {
                ClassInfo info = getClass(curr);
                if (info == null) break;
                if (info.methods.containsKey(methodName)) {
                    List<String> descs = info.methods.get(methodName);
                    if (descriptor == null || descs.contains(descriptor)) {
                        return true;
                    }
                }
                for (String iface : info.interfaces) {
                    if (findMethod(iface, methodName, descriptor)) {
                        return true;
                    }
                }
                curr = info.superClassName;
            }
            return false;
        }
    }

    public static void audit(File projectDir, String projectName) throws Exception {
        String home = System.getProperty("user.home");
        Path loomCache = Paths.get(home, ".gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged");
        List<File> jars = new ArrayList<>();

        if (Files.exists(loomCache)) {
            try (var stream = Files.walk(loomCache)) {
                stream.filter(p -> p.toString().endsWith(".jar") && !p.toString().contains("intermediary") && p.toString().contains("loom.mappings"))
                      .map(Path::toFile)
                      .forEach(jars::add);
            }
        }

        File nonJar = new File(projectDir, "libs/needsofnature-1.5.11.21.11.jar");
        if (nonJar.exists()) {
            jars.add(nonJar);
        }

        JarIndexer indexer = new JarIndexer(jars);
        int totalFiles = 0;
        int totalIssues = 0;

        Pattern mixinPat = Pattern.compile("@Mixin\\s*\\(([^)]+)\\)");
        Pattern injPat = Pattern.compile("@(Inject|Redirect|ModifyArg|ModifyVariable|WrapOperation|WrapWithCondition|ModifyConstant|ModifyReturnValue|ModifyExpressionValue)\\s*\\((.*?)\\)\\s*(?:public|protected|private|static|\\s)+[\\w<>\\[\\],\\s]+\\s+(\\w+)\\s*\\(", Pattern.DOTALL);

        if (!projectDir.exists()) return;

        try (var stream = Files.walk(projectDir.toPath())) {
            List<Path> mixinFiles = stream.filter(p -> p.toString().endsWith(".java") && p.toString().contains("mixin")).toList();
            for (Path p : mixinFiles) {
                totalFiles++;
                String content = Files.readString(p, StandardCharsets.UTF_8);
                Matcher mixinMat = mixinPat.matcher(content);
                if (!mixinMat.find()) continue;

                String targetRaw = mixinMat.group(1);
                List<String> targets = new ArrayList<>();
                Matcher classMat = Pattern.compile("([A-Za-z0-9_]+)\\.class").matcher(targetRaw);
                while (classMat.find()) {
                    String cname = classMat.group(1);
                    if (!"value".equals(cname)) {
                        targets.add(resolveImport(content, cname));
                    }
                }

                Matcher strTargetMat = Pattern.compile("targets\\s*=\\s*\"([^\"]+)\"").matcher(targetRaw);
                while (strTargetMat.find()) {
                    targets.add(strTargetMat.group(1).replace('.', '/'));
                }

                Matcher injMat = injPat.matcher(content);
                while (injMat.find()) {
                    String injType = injMat.group(1);
                    String body = injMat.group(2);
                    Matcher methodMat = Pattern.compile("method\\s*=\\s*\"([^\"]+)\"").matcher(body);
                    if (methodMat.find()) {
                        String targetM = methodMat.group(1);
                        String mName = targetM.contains("(") ? targetM.substring(0, targetM.indexOf('(')) : targetM;
                        String mDesc = targetM.contains("(") ? targetM.substring(targetM.indexOf('(')) : null;

                        boolean found = false;
                        for (String t : targets) {
                            if (indexer.getClass(t) != null && indexer.findMethod(t, mName, mDesc)) {
                                found = true;
                                break;
                            }
                        }

                        if (!targets.isEmpty() && !found && indexer.getClass(targets.get(0)) != null) {
                            System.err.println("  [!] ERROR " + p + ": @" + injType + " target '" + targetM + "' not found in " + targets.get(0));
                            totalIssues++;
                        }
                    }
                }
            }
        }

        System.out.println("[checkMixins] Audited " + totalFiles + " mixin files in " + projectName + ".");
        if (totalIssues > 0) {
            throw new RuntimeException("Found " + totalIssues + " broken mixin targets in " + projectName + "!");
        }
    }

    private static String resolveImport(String content, String simpleName) {
        Pattern imp = Pattern.compile("import\\s+([\\w.]+?\\." + simpleName + ");");
        Matcher m = imp.matcher(content);
        if (m.find()) {
            return m.group(1).replace('.', '/');
        }
        return simpleName;
    }
}

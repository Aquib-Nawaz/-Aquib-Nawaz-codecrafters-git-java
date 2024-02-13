package com.codecrafter.git.clone;

import com.codecrafter.git.Objects.BlobObject;
import com.codecrafter.git.Objects.CommitObject;
import com.codecrafter.git.Objects.TreeLeaf;
import com.codecrafter.git.Objects.TreeObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloneCommand {

    private static String repo;
    public static void git_clone(String[] args) {
        try {
            repo = args[2];
            URL url = new URI(args[1] + "/info/refs?service=git-upload-pack").toURL();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/x-git-upload-pack-advertisement");

            int status = con.getResponseCode();

            if(status != 200)
                throw new Exception("Bad Response");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String header = PktFile.deserialize(in);
            assert header.equals("# service=git_upload_pack");
            PktFile.deserialize(in);

            String capabilities = PktFile.deserialize(in);

            String ref;
            List<String> refs= new ArrayList<>();
            while((ref = PktFile.deserialize(in))!=""){
                refs.add(ref.substring(0,40));
            }
            in.close();

            var response = sendWantRequest(args[1], constructWantRequest(new HashSet<>(refs)));
            PktFile.parsePackfile(new ByteArrayInputStream(response, 0, response.length-20), repo);

            //Objects files are written only populating the real directory is remaining

            for (String nRef:refs){
                CommitObject commitObject = new CommitObject(repo, nRef);
                String treeHash = commitObject.getTree();

                TreeObject tree = new TreeObject(repo, treeHash);
                checkOut(tree, Path.of(repo));
            }
        }
        catch (Exception f){
            f.printStackTrace();
            System.out.printf("CloneException:- %s\n", f.getMessage());
        }
    }

    private static void checkOut(TreeObject tree, Path root_dir) throws IOException{
        for (TreeLeaf entry: tree.getEntries()) {
            switch (entry.mode.length()) {
                case 5:
                    Path subRoot = root_dir.resolve(entry.name);
                    TreeObject subTree = new TreeObject(repo, entry.sha);
                    try {
                        Files.createDirectories(subRoot);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    checkOut(subTree, subRoot);
                    break;

                case 6:
                    BlobObject blob = new BlobObject(repo, entry.sha);
                    Path path = root_dir.resolve(entry.name);

                    try {
                        blob.readObjectFromHash();
                        Files.write(path, blob.getFileContent());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
    }
    private static byte[] constructWantRequest(Set<String> want) throws IOException{
        byte[] write_buffer = new byte[0];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (String obj: want) {
            // format: 0032want 0a53e9ddeaddad63ad106860237bbf53411d11a7\n
            String to_write = "want " + obj;
            to_write += "\n";
            int length = to_write.getBytes().length + 4;
            os.write(String.format("%04x%s", length, to_write).getBytes());
        }
        os.write("00000009done\n".getBytes());
        write_buffer = os.toByteArray();
        return write_buffer;
    }

    private static byte[] sendWantRequest(String repo_url, byte[] write_buffer) throws IOException {
        URL url = new URL(repo_url + "/git-upload-pack");
        HttpURLConnection postHttpURLConnection = (HttpURLConnection) url.openConnection();
        postHttpURLConnection.setDoOutput(true);
        postHttpURLConnection.setRequestMethod("POST");
        postHttpURLConnection.setRequestProperty("Content-Type", "application/x-git-upload-pack-request");
        try (DataOutputStream outputStream = new DataOutputStream(postHttpURLConnection.getOutputStream())) {
            outputStream.write(write_buffer);
        }

        InputStream inputStream = new BufferedInputStream(postHttpURLConnection.getInputStream());
        return inputStream.readAllBytes();
    }


}

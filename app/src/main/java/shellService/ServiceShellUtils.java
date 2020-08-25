package shellService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.List;

public class ServiceShellUtils {
    static final String COMMAND_SU = "su";        // root Command
    static final String COMMAND_SH = "sh";        // sh Command
    static final String COMMAND_EXIT = "exit\n";  // Exit Command
    static final String COMMAND_LINE_END = "\n";  // \n Command

    private ServiceShellUtils(){
        throw new AssertionError();
    }

    // Check Root
    public static boolean checkRootPermission(){
        return execCommand("echo root",true,false).result == 0;
    }

    // String Command To Array Commands,and isNeedResultMsg is true
    public static ServiceShellUtils.ServiceShellCommandResult execCommand(String command,boolean isRoot){
        return execCommand(new String[]{command},isRoot,true);
    }

    // String Command To Array Commands
    public static ServiceShellUtils.ServiceShellCommandResult execCommand(String command,boolean isRoot,boolean isNeedResultMsg){
        return execCommand(new String[]{command},isRoot,isNeedResultMsg);
    }

    // List Commands To Array Commands,and isNeedResultMsg is true
    public static ServiceShellUtils.ServiceShellCommandResult execCommand(List<String> commands,boolean isRoot){
        return execCommand(commands == null ? null : commands.toArray(new String[]{}),isRoot,true);
    }

    // List Commands To Array Commands
    public static ServiceShellUtils.ServiceShellCommandResult execCommand(List<String> commands,boolean isRoot,boolean isNeedResultMsg){
        return execCommand(commands == null ? null : commands.toArray(new String[]{}),isRoot,isNeedResultMsg);
    }

    // isNeedResultMsg is true
    public static ServiceShellUtils.ServiceShellCommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    public static ServiceShellUtils.ServiceShellCommandResult execCommand(String[] commands,boolean isRoot,boolean isNeedResultMsg){
        System.out.println("Run execCommand");
        int result = -1;
        if(commands == null || commands.length == 0){
            return new ServiceShellCommandResult(result,null,null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuffer successMsg = null;
        StringBuffer errorMsg = null;
        DataOutputStream os = null;
        String s;

        try{
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());

            for(String command : commands){
                if(command.isEmpty()){
                    continue;
                }

                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }

            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();

            // get command Msg
            if(isNeedResultMsg){
                successMsg = new StringBuffer();
                errorMsg = new StringBuffer();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                while((s = successResult.readLine()) != null){
                    successMsg.append(s);
                }

                while((s = errorResult.readLine()) != null){
                    errorMsg.append(s);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(os != null){
                    os.close();
                }
                if(successResult != null){
                    successResult.close();
                }
                if(errorResult != null){
                    errorResult.close();
                }
                if(process != null){
                    process.destroy();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        return new ServiceShellUtils.ServiceShellCommandResult(
                result,
                successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }

    public static class ServiceShellCommandResult{
        int result;
        String successMsg;
        String errorMsg;

        public ServiceShellCommandResult(int result){
            this.result=result;
        }

        public ServiceShellCommandResult(int result,String successMsg,String errorMsg){
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}

/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.client;

import io.nuls.client.constant.CommandConstant;
import io.nuls.client.processor.AccountProcessors;
import io.nuls.client.processor.BlockProcessors;
import io.nuls.client.processor.SystemProcessors;
import io.nuls.client.processor.WalletProcessors;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.sdk.SdkManager;

import java.io.IOException;
import java.util.*;

/**
 * @author Niels
 * @date 2017/10/26
 */
public class CommandHandler {

    public static final Map<String, CommandProcessor> PROCESSOR_MAP = new HashMap<>();

    private void init() {
        //todo 在这里注册所有的命令处理器
        register(new SystemProcessors.Exit());
        register(new SystemProcessors.Help());
        register(new SystemProcessors.Version());
        register(new AccountProcessors.CreateAccount());
        register(new BlockProcessors.BestHeight());
        register(new WalletProcessors.Transfer());
        //todo 修改为配置
        SdkManager.init("http://127.0.0.1:8001/nuls");
    }

    public static void main(String[] args) throws IOException {
        CommandHandler instance = new CommandHandler();
        instance.init();
        System.out.print(CommandConstant.COMMAND_PS1);
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            String read = scan.nextLine().trim();
            if (StringUtils.isBlank(read)) {
                System.out.print(CommandConstant.COMMAND_PS1);
                continue;
            }
            System.out.print(CommandConstant.COMMAND_PS1 + instance.processCommand(read.split(" ")) + "\n" + CommandConstant.COMMAND_PS1);
        }
    }

    private String processCommand(String[] args) {
        if (args.length == 0) {
            return CommandConstant.COMMAND_ERROR;
        }
        String command = args[0];
        CommandProcessor processor = PROCESSOR_MAP.get(command);
        if (processor == null) {
            return command + " not a nuls command!";
        }
        try {
            boolean result = processor.argsValidate(args);
            if (!result) {
                return "args not right:::" + processor.getCommandDescription();
            }
            return processor.execute(args).toString();
        } catch (Exception e) {
            return CommandConstant.COMMAND_ERROR + ":" + e.getMessage();
        }
    }


    private void register(CommandProcessor processor) {
        PROCESSOR_MAP.put(processor.getCommand(), processor);
    }
}

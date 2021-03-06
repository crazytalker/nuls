/*
 *
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.client.processor;

import io.nuls.account.entity.Account;
import io.nuls.client.entity.CommandResult;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.resources.form.TransferForm;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.WalletService;

/**
 * @author Niels
 * @date 2018/3/7
 */
public abstract class WalletProcessors implements CommandProcessor {

    protected WalletService walletService = new WalletService();














    /**
     * nuls transfer
     */
    public static class Transfer extends WalletProcessors {

        private ThreadLocal<TransferForm> paramsData = new ThreadLocal<>();

        @Override
        public String getCommand() {
            return "transfer";
        }

        @Override
        public String getCommandDescription() {
            return "transfer <address> <toAddress> <amount> <password> <remark> --toAddress$amount&password are required";
        }

        @Override
        public boolean argsValidate(String[] args) {
            boolean result;
            do {
                result = args.length >= 2;
                if (!result) {
                    break;
                }
                TransferForm form = getTransferForm(args);
                paramsData.set(form);
                result = StringUtils.isNotBlank(form.getToAddress());
                if (!result) {
                    break;
                }
                result = form.getAmount() != null && form.getAmount() > 0;
            } while (false);
            return result;
        }

        private TransferForm getTransferForm(String[] args) {
            TransferForm form = new TransferForm();
            switch (args.length) {
                case 3:
                    form.setToAddress(args[0]);
                    form.setAmount(Na.parseNuls(args[1]).getValue());
                    form.setPassword(args[2]);
                    break;
                case 4:
                    Double amount = getDoubleAmount(args[1]);
                    if (null == amount) {
                        form.setAddress(args[0]);
                        form.setToAddress(args[1]);
                        amount = getDoubleAmount(args[2]);
                        form.setAmount(Na.parseNuls(amount).getValue());
                        form.setPassword(args[3]);
                    } else {
                        form.setToAddress(args[0]);
                        form.setAmount(Na.parseNuls(amount).getValue());
                        form.setPassword(args[2]);
                        form.setRemark(args[3]);
                    }
                    break;
                case 5:
                    form.setAddress(args[0]);
                    form.setToAddress(args[1]);
                    form.setAmount(Na.parseNuls(args[2]).getValue());
                    form.setPassword(args[3]);
                    form.setRemark(args[4]);
                    break;
            }
            return form;
        }


        private Double getDoubleAmount(String arg) {
            try {
                return Double.parseDouble(arg);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public CommandResult execute(String[] args) {
            TransferForm form = paramsData.get();
            if (null == form) {
                form = getTransferForm(args);
            }
            RpcClientResult result = this.walletService.transfer(form.getAddress(), form.getPassword(), form.getToAddress(), form.getAmount(), form.getRemark());
            return CommandResult.getResult(result);
        }
    }


}

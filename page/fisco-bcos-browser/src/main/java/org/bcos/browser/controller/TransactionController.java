/*
 * This file is part of FISCO BCOS Browser.
 *
 * FISCO BCOS Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FISCO BCOS Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FISCO BCOS Browser.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * @file: TransactionController.java
 * @author: v_wbsqwu
 * @date: 2018
 */

package org.bcos.browser.controller;


import org.bcos.browser.base.BaseController;
import org.bcos.browser.base.BcosBrowserException;
import org.bcos.browser.base.ConstantCode;
import org.bcos.browser.base.Constants;
import org.bcos.browser.base.utils.BrowserUtils;
import org.bcos.browser.base.utils.DateTimeUtils;
import org.bcos.browser.dto.TbTransactionDto;
import org.bcos.browser.entity.base.BasePageRespEntity;
import org.bcos.browser.entity.base.BaseRspEntity;
import org.bcos.browser.entity.req.ReqGetTbTransactionByPkHashVO;
import org.bcos.browser.entity.req.ReqGetTbTransactionInfoByPageVO;
import org.bcos.browser.entity.rsp.RspTbTransactionInfoVO;
import org.bcos.browser.service.TbTransactionService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "transaction")
public class TransactionController extends BaseController {
    private static Logger LOGGER =  LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    TbTransactionService tbTransactionService;

    /**
     *@Description: Jump trading information page
     */
    @RequestMapping(value = "/transaction.page",method = RequestMethod.GET)
    public ModelAndView toTransactionPage(Integer blockHeight){
        LOGGER.info("toTransactionPage.start blockNumber:{}",blockHeight);

        ModelAndView mav = new ModelAndView();
        mav.setViewName("transaction");
        mav.addObject("blockHeight",blockHeight==null?"":blockHeight);
        LOGGER.info("toTransactionPage.end response:{}", JSON.toJSONString(mav));
        return mav;
    }


    /**
     *@Description:Pagination query transaction information table data
     */
    @ResponseBody
    @RequestMapping(value = "/getTbTransactionInfoByPage.json",method = RequestMethod.POST)
    public BasePageRespEntity getTbTransactionInfoByPage(@Valid @RequestBody ReqGetTbTransactionInfoByPageVO reqEntity, BindingResult result){
        LOGGER.info("getTbTransactionInfoByPage.start reqEntity:{}",JSON.toJSONString(reqEntity));
        checkParamResult(result);

        Map<String,Object> map = new HashedMap();
        map.put("blockHeight",reqEntity.getBlockHeight());
        map.put("hash",BrowserUtils.trimSpaces(reqEntity.getHash()));
        map.put("dateTime1",reqEntity.getDateTime1());
        map.put("dateTime2",reqEntity.getDateTime2());
        map.put("offset",reqEntity.getStart());
        map.put("size",reqEntity.getPageSize());

        int total = tbTransactionService.getAllTransactionCount(map);

        List<RspTbTransactionInfoVO> list = null;

        if(total>0){
            List<TbTransactionDto> listTbTransactionDto = tbTransactionService.getTbTransactionByOffset(map);
            if(listTbTransactionDto!=null){
                list = new ArrayList<>();
                for(TbTransactionDto tbTransactionDto:listTbTransactionDto){
                    RspTbTransactionInfoVO rspEntity = new RspTbTransactionInfoVO();
                    rspEntity.setPkHash(tbTransactionDto.getPkHash());//	TxHash
                    rspEntity.setBlockHash(tbTransactionDto.getBlockHash());//		VARCHAR	32
                    rspEntity.setBlockNumber(tbTransactionDto.getBlockNumber());//
                    rspEntity.setBlockTimesStr(DateTimeUtils.Timestamp2String(tbTransactionDto.getBlockTimestamp(), Constants.DEFAULT_DATA_TIME_FORMAT));
                    rspEntity.setBlockGasLimit(tbTransactionDto.getBlockGasLimit());//Gas Limit
                    rspEntity.setTransactionIndex(tbTransactionDto.getTransactionIndex());
                    rspEntity.setTransactionFrom(tbTransactionDto.getTransactionFrom());
                    rspEntity.setTransactionTo(tbTransactionDto.getTransactionTo());
                    rspEntity.setGas(tbTransactionDto.getGas());//Used By Txn
                    rspEntity.setGasPrice(tbTransactionDto.getGasPrice());
                    rspEntity.setCumulativeGas(tbTransactionDto.getCumulativeGas());
                    rspEntity.setRandomId(tbTransactionDto.getRandomId());
                    rspEntity.setInputText(tbTransactionDto.getInputText());//Input Data

                    list.add(rspEntity);
                }
            }
        }

        BasePageRespEntity response = new BasePageRespEntity();
        response.setRetCode(ConstantCode.SUCCESS);
        response.setPageNumber(reqEntity.getPageNumber());
        response.setPageSize(reqEntity.getPageSize());
        response.setTotal(total);
        response.setList(list);

        LOGGER.info("getTbTransactionInfoByPage.end response:{}", JSON.toJSONString(response));
        return response;
    }

    /**
     *@Description:Open transaction details page
     */
    @RequestMapping(value = "/getTbTransactionDetailPage.page",method = RequestMethod.GET)
    public ModelAndView getTbTransactionDetailPage(String pkHash){
        LOGGER.info("getTbTransactionDetailPage.start pkHash:{}",pkHash);
        if(StringUtils.isBlank(pkHash)){
            throw new BcosBrowserException(ConstantCode.QUERY_FAIL_PK_HASH_EMPTY);
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("transactionDetail");
        mav.addObject("pkHash",pkHash);
        LOGGER.info("getTbTransactionDetailPage end,response:{}", JSON.toJSONString(mav));
        return mav;
    }

    /**
     *@Description:According to pkHash query transaction receipt information
     */
    @ResponseBody
    @RequestMapping(value = "/getTbTransactionByPkHash.json",method = RequestMethod.POST)
    public BaseRspEntity getTbTransactionByPkHash(@Valid @RequestBody ReqGetTbTransactionByPkHashVO reqEntity, BindingResult result){
        LOGGER.info("getTbTransactionByPkHash.start reqEntity:{}", JSON.toJSONString(reqEntity));
        checkParamResult(result);

        TbTransactionDto tbTransactionDto = tbTransactionService.getTbTransactionByPkHash(reqEntity.getPkHash());

        BaseRspEntity response = new BaseRspEntity(ConstantCode.SUCCESS);
        response.setData(tbTransactionDto);

        LOGGER.info("getTbTransactionByPkHash.end response:{}", JSON.toJSONString(response));
        return response;
    }


}
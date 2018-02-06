package com.nowbook.sdp.model;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;

public class UserWalletSummary extends PageModel{

    private static final long serialVersionUID = 6404288790255635091L;

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private Long userId;//用户ID

    @Setter
    @Getter
    private Long otherId;//小伙伴Id

    @Setter
    @Getter
    private String otherName;//小伙伴名称

    @Setter
    @Getter
    private Integer type;//类型

    @Setter
    @Getter
    private Long money;//金额

    @Setter
    @Getter
    private Long otherMoney;//小伙伴消耗的钱

    @Setter
    @Getter
    private Integer level;

    @Setter
    @Getter
    private Integer otherLevel;

    @Setter
    @Getter
    private Integer type1;

    @Setter
    @Getter
    private Integer type2;

    @Setter
    @Getter
    private String day;

    @Setter
    @Getter
    private String time;

    @Setter
    @Getter
    private String message;

    @Setter
    @Getter
    private String realName;

    @Setter
    @Getter
    private String mobile;

    @Setter
    @Getter
    private String nick;

    @Setter
    @Getter
    private Long orderItemId;

    @Setter
    @Getter
    private Long realMoney;//商品全价

    @Setter
    @Getter
    private Integer moneyType;//明细来源类型 1：自营 2：优选 3：入会或者升级 4:自定义

    @Setter
    @Getter
    private Integer payType;//支付方式 3支付宝 4微信 5银联

    @Setter
    @Getter
    private String payCode;//流水号
    @Setter
    @Getter
    private JSONObject json;

    public void makeMessage(){
        switch (type){
            //余额
            case 1:
                //充值升级时余额增加
                message = "您升级到【"+ Level.LEVEL.fromNumber(level).toString()+"】，余额：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 3:
                //退款时余额增加
                message = "您退款商品金额："+String.format("%.2f",Double.valueOf(money)/100)+"元，余额：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 5:
                //退款时余额增加，并伴随着升级
                message = "您退款商品金额："+String.format("%.2f",Double.valueOf(money)/100)+"元，但由于您已经升级到【"+ Level.LEVEL.fromNumber(level).toString()+"】，余额：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 6:
                //自己消费自营商品时余额减少
                message = "您购买商品金额："+String.format("%.2f",Double.valueOf(money)/100)+"元，余额："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 8:
                //升级余额换算时余额减少
                message = "您升级到【"+ Level.LEVEL.fromNumber(level).toString()+"】，余额按照折扣换算："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 10:
                //下级升级向你进货时余额减少
                message = "您的小伙伴【"+otherName+"】升级到【"+ Level.LEVEL.fromNumber(otherLevel).toString()+"】向您进货："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您的余额："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 12:
                //下级购买商品向你进货时余额减少
                message = "您的小伙伴【"+otherName+"】购买商品，金额："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您的余额："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //预存款
            case 21:
                //自己充值时预存款增加
                message = "您通过充值预存款：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 23:
                //自己充值升级时预存款增加
                message = "您升级到【"+ Level.LEVEL.fromNumber(level).toString()+"】，预存款：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 25:
                //退款时预存款增加
                message = "您退款商品金额："+String.format("%.2f",Double.valueOf(money)/100)+"元，预存款：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 26:
                //自己消费时预存款减少
                message = "您购买商品金额："+String.format("%.2f",Double.valueOf(money)/100)+"元，预存款："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //保证金
            case 31:
                //自己充值升级时保证金增加
                message = "您升级到【"+ Level.LEVEL.fromNumber(level).toString()+"】，保证金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 33:
                //合伙人每期奖励使保证金增加
                message = "由于您属于【合伙人】，从这期奖励中抽出一部分加入保证金，保证金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 34:
                //自己退会时保证金减少
                message = "您退出了钱唐荟会员，保证金："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //待发放收益
            case 41:
                //下级升级进货时待发放收益增加
                message ="您的小伙伴【"+otherName+"】升级到【"+ Level.LEVEL.fromNumber(otherLevel).toString()+"】向您进货："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您的待发放收益：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 43:
                //下级购买商品时待发放收益增加
                message="您的小伙伴【"+otherName+"】购买商品，金额："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您的待发放收益：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 44:
                //下级退货时待发放收益减少
                message="您的小伙伴【"+otherName+"】退货，金额："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您的待发放收益："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 46:
                //结算过后待发放收益减少
                message="待发放收益已结算，待发放收益："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //待发放奖金
            case 51:
                //被你推荐的人升级进货时待发放奖金增加
                message="您推荐的小伙伴【"+otherName+"】升级到【"+ Level.LEVEL.fromNumber(otherLevel).toString()+"】，您的待发放奖金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 53:
                //被你推荐的人购买商品时待发放奖金增加
                message="您推荐的小伙伴【"+otherName+"】购买商品，金额："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您的待发放奖金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 55:
                //合伙人的推荐人获得的伯乐奖
                message="您推荐的【合伙人】小伙伴【"+otherName+"】获得业绩："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您获得伯乐奖奖金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 57:
                //合伙人的推荐人获得的伯乐奖
                message="您推荐的【合伙人】小伙伴【"+otherName+"】购买商品，金额："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您获得伯乐奖奖金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 58:
                //被你推荐的人退货时待发放奖金减少
                message="您推荐的小伙伴【"+otherName+"】退货，金额："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您的待发放奖金："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 60:
                //结算后待发放奖金减少
                message="待发放奖金已结算，待发放奖金："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //待支付邮费
            case 61:
                //自己购买商品时待支付邮费增加
                message="您购买商品，待支付邮费：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 62:
                //结算支付时待支付邮费减少
                message="待支付邮费已支付，待支付邮费："+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //已结算收益
            case 71:
                message="您的累计收益：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //已结算奖金
            case 81:
                message="您的累计奖金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            //已支付邮费
            case 91:
                message="您的累计支付邮费：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
        }
    }

    public void makePushMessage() {
        switch (type) {
            //余额
            case 1:
                //充值升级时余额增加
                message = "您升级到【" + Level.LEVEL.fromNumber(level).toString() + "】，余额：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 3:
                //退款时余额增加
                message = "您退款商品金额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元，余额：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 5:
                //退款时余额增加，并伴随着升级
                message = "您退款商品金额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元，但由于您已经升级到【" + Level.LEVEL.fromNumber(level).toString() + "】，余额：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 6:
                //自己消费自营商品时余额减少
                message = "您购买商品金额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元，余额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 8:
                //升级余额换算时余额减少
                message = "您升级到【" + Level.LEVEL.fromNumber(level).toString() + "】，余额按照折扣换算：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 10:
                //下级升级向你进货时余额减少
                message = "您的小伙伴【" + otherName + "】升级到【" + Level.LEVEL.fromNumber(otherLevel).toString() + "】向您进货：" + String.format("%.2f", Double.valueOf(otherMoney) / 100) + "元，您的余额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 12:
                //下级购买商品向你进货时余额减少
                message = "您的小伙伴【" + otherName + "】购买商品，金额：" + String.format("%.2f", Double.valueOf(otherMoney) / 100) + "元，您的余额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //预存款
            case 21:
                //自己充值时预存款增加
                message = "您通过充值预存款：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 23:
                //自己充值升级时预存款增加
                message = "您升级到【" + Level.LEVEL.fromNumber(level).toString() + "】，预存款：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 25:
                //退款时预存款增加
                message = "您退款商品金额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元，预存款：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 26:
                //自己消费时预存款减少
                message = "您购买商品金额：" + String.format("%.2f", Double.valueOf(money) / 100) + "元，预存款：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //保证金
            case 31:
                //自己充值升级时保证金增加
                message = "您升级到【" + Level.LEVEL.fromNumber(level).toString() + "】，保证金：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 33:
                //合伙人每期奖励使保证金增加
                message = "由于您属于【合伙人】，从这期奖励中抽出一部分加入保证金，保证金：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 34:
                //自己退会时保证金减少
                message = "您退出了钱唐荟会员，保证金：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //待发放收益
            case 41:
                //下级升级进货时待发放收益增加
                message = "您的小伙伴【" + otherName + "】升级到【" + Level.LEVEL.fromNumber(otherLevel).toString() + "】向您进货：" + String.format("%.2f", Double.valueOf(otherMoney) / 100) + "元，您的待发放收益：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 43:
                //下级购买商品时待发放收益增加
                message = "您的小伙伴【" + otherName + "】购买商品，金额：" + String.format("%.2f", Double.valueOf(otherMoney) / 100) + "元，您的待发放收益：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 44:
                //下级退货时待发放收益减少
                message = "您的小伙伴【" + otherName + "】退货，金额：" + String.format("%.2f", Double.valueOf(otherMoney) / 100) + "元，您的待发放收益：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 46:
                //结算过后待发放收益减少
                message = "待发放收益已结算，待发放收益：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //待发放奖金
            case 51:
                //被你推荐的人升级进货时待发放奖金增加
                message = "您推荐的小伙伴【" + otherName + "】升级到【" + Level.LEVEL.fromNumber(otherLevel).toString() + "】，您的待发放奖金：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 53:
                //被你推荐的人购买商品时待发放奖金增加
                message = "您推荐的小伙伴【" + otherName + "】购买商品，金额：" + String.format("%.2f", Double.valueOf(otherMoney) / 100) + "元，您的待发放奖金：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 55:
                //合伙人的推荐人获得的伯乐奖
                message="您推荐的【合伙人】小伙伴【"+otherName+"】获得业绩："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您获得伯乐奖奖金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 57:
                //合伙人的推荐人获得的伯乐奖
                message="您推荐的【合伙人】小伙伴【"+otherName+"】获得业绩："+String.format("%.2f",Double.valueOf(otherMoney)/100)+"元，您获得伯乐奖奖金：+"+String.format("%.2f",Double.valueOf(money)/100)+"元。";
                break;
            case 58:
                //被你推荐的人退货时待发放奖金减少
                message = "您推荐的小伙伴【" + otherName + "】退货，金额：" + String.format("%.2f", Double.valueOf(otherMoney) / 100) + "元，您的待发放奖金：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 60:
                //结算后待发放奖金减少
                message = "待发放奖金已结算，待发放奖金：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //待支付邮费
            case 61:
                //自己购买商品时待支付邮费增加
                message = "您购买商品，待支付邮费：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            case 62:
                //结算支付时待支付邮费减少
                message = "待支付邮费已支付，待支付邮费：" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //已结算收益
            case 71:
                message = "您的累计收益：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //已结算奖金
            case 81:
                message = "您的累计奖金：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
            //已支付邮费
            case 91:
                message = "您的累计支付邮费：+" + String.format("%.2f", Double.valueOf(money) / 100) + "元。";
                break;
        }
    }

}
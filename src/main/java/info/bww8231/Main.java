/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package info.bww8231;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * このクラスに静的変数を追加したり、初期化を行ったりしないでください。
 * 実行内容がわからない場合は、パラメータークラスをstartRobot呼び出しに変更する場合を除いて、
 * このファイルを変更しないでください。
 */
public final class Main
{
    private Main() {}

    /**
     * メイン関数の初期化メソッド。 ここでは初期化を追加しないでください。
     * メインのRobotクラス（名前）を変更する場合は、パラメーターの種類を変更します。
     */
    public static void main(String... args)
    {
        RobotBase.startRobot(Robot::new);
    }
}

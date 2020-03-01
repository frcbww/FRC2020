/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package info.bww8231;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.cameraserver.CameraServer;

/**
 * VMは、このクラスを自動的に実行し、TimedRobotのドキュメントに記載されている各モードに対応するメソッドです。
 * このプロジェクトの作成後にこのクラスまたはパッケージの名前を変更する場合は、プロジェクトのbuild.gradleファイルも更新する必要があります。
 */
public class Robot extends TimedRobot {
    private final DifferentialDrive robotDrive = new DifferentialDrive(new PWMVictorSPX(0), new PWMVictorSPX(1));
    private final PWMVictorSPX launchLMove = new PWMVictorSPX(2);
    private final PWMVictorSPX launchRight = new PWMVictorSPX(3);
    private final PWMVictorSPX launchLeft = new PWMVictorSPX(4);
    private final PWMVictorSPX collect = new PWMVictorSPX(5);
    private final PWMVictorSPX collectMove = new PWMVictorSPX(6);
    private final PWMVictorSPX beltConveyor = new PWMVictorSPX(7);

    private final Joystick stick = new Joystick(0);
    private final Joystick signDate = new Joystick(0);
    private final Timer timer = new Timer();
    private final Solenoid solenoid = new Solenoid(0);
    private final ADXRS450_Gyro gyro = new ADXRS450_Gyro();

    /**
     * このメソッドはロボットが最初に起動されたときに実行され、初期化コードを書くことができます。
     */
    @Override
    public void robotInit() {
        CameraServer . getInstance (). startAutomaticCapture (0);
        CameraServer . getInstance (). startAutomaticCapture (1);
        gyro.calibrate();
        gyro.reset();
    }

    /**
     *  このメソッドは、モードに関係なく、すべてのロボットパケットと呼ばれます。
     *  無効、自律、遠隔操作、およびテスト中に実行する診断などの項目にこれを使用します。
     *
     *  これは、モード固有の定期的な方法の後、LiveWindowとSmartDashboardの統合更新の前に実行されます。
     */
    @Override
    public void robotPeriodic() {
    }

    /**
     * この自律型（上記の選択コード）は、ダッシュボードを使用して異なる自律型モードを選択するための方法を示しています。
     * また、送信可能な選択コードは、Java SmartDashboardで機能します。
     *
     * LabVIEWダッシュボードを使用する場合は、すべての選択コードを削除し getString コードのコメントを解除して、
     * ジャイロの下のテキストボックスから自動名(auto name)を取得します。
     *
     * 上記のセレクターコードに追加コマンド（コメントに載っているの例など）を追加するか、
     * 以下のスイッチ構造に追加の文字列とコマンドを追加して比較することにより、自動モードを追加できます。
     */
    @Override
    public void autonomousInit() {
        timer.reset();
        timer.start();
    }

    /**
     * このメソッドは、自律中に定期的に呼び出されます。
     */
    @Override
    public void autonomousPeriodic() {
        // 前進プログラムテスト
        if (timer.get() < 5.0){
            robotDrive.arcadeDrive(0.6, (gyro.getAngle() / -15)); // ジャイロによる直進の補正
        } else {
            robotDrive.stopMotor();
        }
    }

    @Override
    public void teleopInit() {
    }

    /**
     * このメソッドは、操作制御中に定期的に呼び出されます。
     */
    @Override
    public void teleopPeriodic() {
        int sign = 1;   //反転係数
        double speedRate = (-1 * signDate.getRawAxis(3) + 1) * 0.5; //速度係数
        double launchSpeed = 0.5;

        //コントローラーデータ
        double stickX = 0.2 * stick.getX();
        double stickY = -1 * stick.getY();
        double stickZ = 0.6 * stick.getZ();

        double stickFB = stickY;
        double stickLR = Math.abs(stickX + stickZ) >= 0.6 ? stickZ : stickX + stickZ;    //スピードを0.5以上にさせない

        //高速モード Button11
        if (stick.getRawButton(11)) {
            launchSpeed = 1;
        } else {
            launchSpeed = 0.6;
        }

        //反転モード Button12
        if (stick.getRawButton(12)) {
            sign = -1;
        } else {
            sign = 1;
        }

        //発射機構 Button1
        if (stick.getRawButton(1)) {
            launchRight.set(launchSpeed * speedRate * sign);
            launchLeft.set(-1 * launchSpeed * speedRate * sign);
            launchLMove.set(-0.5 * sign);
        } else {
            launchLMove.set(0);
            launchRight.set(0);
            launchLeft.set(0);
        }

        //回収機構 Button2
        if (stick.getRawButton(2)) {
            solenoid.set(true);
            collect.set(0.2 * sign);
        } else {
            solenoid.set(false);
            collect.set(0);
        }
        //ベルトコンベア Button7
        if (stick.getRawButton(7)) {
            beltConveyor.set(0.7 * sign);
        } else {
            beltConveyor.set(0);
        }
        //足回り角度の微調整モード Button8
        if (stick.getRawButton(8)) {
            //明日調整
            robotDrive.arcadeDrive(0, 0);
        } else {
            robotDrive.arcadeDrive(stickFB * speedRate, stickLR * speedRate, true);
        }

        //回収機構（常に回転）
        collectMove.set(-0.7 * sign);
    }

    /**
     * このメソッドは、テストモード中に定期的に呼び出されます。
     */
    @Override
    public void testPeriodic() {
    }
}
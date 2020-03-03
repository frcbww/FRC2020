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

import java.io.UnsupportedEncodingException;

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
    private final Compressor compressor = new Compressor();
    private final Solenoid solenoid = new Solenoid(0);
    private final ADXRS450_Gyro gyro = new ADXRS450_Gyro();
    private final SerialPort serial = new SerialPort(9600,SerialPort.Port.kUSB);
//    private final Encoder beltEncoder = new Encoder(0, 1);

    private final PID pid = new PID();

    private boolean isGyroInit = false; //ジャイロ初期化
    private boolean isStraight = false; //直進モードの状態
    private int sign = 1; //反転係数
    private double speedRate; //速度係数
    private double launchSpeed = 0.5; //発射機構の速度係数

    /**
     * このメソッドはロボットが最初に起動されたときに実行され、初期化コードを書くことができます。
     */
    @Override
    public void robotInit() {
        CameraServer . getInstance (). startAutomaticCapture (0);
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
        if (timer.get() < 1.0){
            straightPID(0.5, gyro.getAngle());
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
        //コントローラーデータ
        double stickX = 0.2 * stick.getX();
        double stickZ = 0.6 * stick.getZ();
        double stickY = stick.getY();

        double stickLR = stickX + stickZ;
        double stickAbsXZ = Math.abs(stickX + stickZ);

        if (stickAbsXZ >= 0.6) {
            //スピード制限
            stickLR = stickZ;
        }

        //速度係数
        speedRate = (-1 * signDate.getRawAxis(3) + 1) * 0.5;

        //Arduinoとのシリアル通信
        arduinoSubmit();
        switch (arduinoReceiving()) {
            case 0:
                System.out.println("----None---- (Status: 0)");
                break;
            case 1:
//                beltConveyor.set(0.5);
                System.out.println("----Collect---- (Status: 1)");
                break;
            case 2:
                System.out.println("----STOP---- (Status: 2)");
                break;
            case 3:
                System.out.println("---Collect & STOP--- (Status: 3)");
                break;
        }

        //高速モード Button4
        if (stick.getRawButton(4)) {
            launchSpeed = 1;
        } else {
            launchSpeed = 0.5;
        }

        //反転モード Button12
        if (stick.getRawButton(12)) {
            sign = -1;
        } else {
            sign = 1;
        }

        //発射機構＆ベルトコンベア Button1
        if (stick.getRawButton(1)) {
            beltConveyor.set(0.8 * sign);
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

        //ベルトコンベア Button7（仮）
        if (stick.getRawButton(7)) {
            beltConveyor.set(0.7 * sign);
        }

        //微調整モード Button3
        if (stick.getRawButton(3)) {
            robotDrive.arcadeDrive(0, 0.6 * stickLR);
            isGyroInit = false;
        } else if (stickAbsXZ <= 0.003 && !isStraight) {
            //直進モード
            gyroInit();
            straightPID(stickY * speedRate, gyro.getAngle());
        } else {
            //足回りモーター
            robotDrive.arcadeDrive(stickY * speedRate, stickLR * speedRate, true);
            isGyroInit = false;
        }

        //ベルトコンベアの停止
        if (!stick.getRawButton(1) && !stick.getRawButton(7)) {
            beltConveyor.set(0);
        }

        //回収機構（常に回転）
        collectMove.set(-0.7 * sign);

        //直進モードのON/OFF
        if (stickAbsXZ > 0.003) {
            isStraight = true;
        } else if (stickAbsXZ == 0) {
            isStraight = false;
        }
    }


    //ジャイロの初期化
    public void gyroInit() {
        if (!isGyroInit) {
            gyro.reset();
            isGyroInit = true;
        }
    }

    //PID直進
    public void straightPID(double speed, double nowAngle) {
        pid.setGain(0.05,0.00002,0.3);
        pid.setTarget(0);

        double val = pid.getCalculation(nowAngle);
        robotDrive.arcadeDrive(speed,val);
    }

    //PID直角カーブ
    public void anglePID(double speed, double nowAngle, boolean left) {
        int sign;

        if (left) {
            sign = -1;
        } else {
            sign = 1;
        }

        pid.setGain(0.04,0.00001,0.07);
        pid.setTarget(90 * sign);

        double val = pid.getCalculation(nowAngle);
        robotDrive.arcadeDrive(speed,val);
    }

    @Override
    public void testInit() {
    }

    /**
     * このメソッドは、テストモード中に定期的に呼び出されます。
     */
    @Override
    public void testPeriodic() {
    }

    public void arduinoSubmit() {
        final byte[] date = "1".getBytes();
        serial.write(date, 1);
    }

    public int arduinoReceiving() {
        byte[] date = serial.read(1);

        try {
            String str = new String(date, "US-ASCII");
            int integer = Integer.parseInt(str);
            return integer;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
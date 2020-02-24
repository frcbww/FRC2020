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
    private final DifferentialDrive m_robotDrive = new DifferentialDrive(new PWMVictorSPX(0), new PWMVictorSPX(1));
    private final PWMVictorSPX launchLMove = new PWMVictorSPX(2);
    private final PWMVictorSPX launchRight = new PWMVictorSPX(3);
    private final PWMVictorSPX launchLeft = new PWMVictorSPX(4);
    private final PWMVictorSPX collect = new PWMVictorSPX(5);

    private final Joystick m_stick = new Joystick(0);
    private final Timer m_timer = new Timer();
    private final Compressor c = new Compressor(0);
    private final Solenoid exampleSolenoid = new Solenoid(1);

    /**
     * このメソッドはロボットが最初に起動されたときに実行され、初期化コードを書くことができます。
     */
    @Override
    public void robotInit() {
        CameraServer . getInstance (). startAutomaticCapture ();
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
        m_timer.reset();
        m_timer.start();
    }

    /**
     * このメソッドは、自律中に定期的に呼び出されます。
     */
    @Override
    public void autonomousPeriodic() {
        // 前進プログラムテスト
//        if (m_timer.get() < 3.0) {
//            m_robotDrive.arcadeDrive(0.5, 0.0); // drive forwards half speed
//        } else {
//            m_robotDrive.stopMotor(); // stop robot
//        }
    }

    @Override
    public void teleopInit() {
        c.setClosedLoopControl(true);

//        boolean enabled = c.enabled();
//        boolean pressureSwitch = c.getPressureSwitchValue();
//        double current = c.getCompressorCurrent();
    }

    /**
     * このメソッドは、操作制御中に定期的に呼び出されます。
     */
    @Override
    public void teleopPeriodic() {
        double stickX = 0.8 * m_stick.getX();
        double stickY = -1 * m_stick.getY();

        if (m_stick.getRawButton(1)) {
            launchLMove.set(-0.5);
            launchRight.set(1);
            launchLeft.set(1);
        } else {
            launchLMove.set(0);
            launchRight.set(0);
            launchLeft.set(0);
        }

        if (m_stick.getRawButton(2)) {
            collect.set(0.3);
        } else {
            collect.set(0);
        }

        if (m_stick.getRawButton(3)) {
            c.setClosedLoopControl(false);
            exampleSolenoid.set(true);
        } else {
            exampleSolenoid.set(false);
        }

        m_robotDrive.arcadeDrive(stickY, stickX);
    }

    /**
     * このメソッドは、テストモード中に定期的に呼び出されます。
     */
    @Override
    public void testPeriodic() {
    }
}
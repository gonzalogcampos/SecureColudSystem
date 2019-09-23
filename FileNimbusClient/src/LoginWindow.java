import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class LoginWindow {
	java.io.File chosenFile;
	JFrame frmFilenimbus;
	JPanel loginPanel, signUpPanel, userPanel, panelSettings;

	JLabel statLabel, lblUser, lblOldname;
	JTextField userField, userRegister, txtNewuser;
	JPasswordField passField, passRegister1, passRegister2, passNewpassword, passRepeatpassword;
	
	JTable table;
	DefaultTableModel model;

	final int portNum = 8080;
	final String ip = "localhost";
	final Client mainClient;
	
	// Acciones
	final byte DOWNLOAD = 0;
	final byte DELETE = 1;
	final byte SHARE = 2;
	
	// Tamanyo ventana
	final short WIDTH = 720;
	final short HEIGHT = 480;
	
	final String NO_FILE_SELECTED = "No file selected";
	
	
	/**
	 * Create the application.
	 */
	public LoginWindow() {
		mainClient = new Client(portNum, ip);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFilenimbus = new JFrame();
		frmFilenimbus.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		frmFilenimbus.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		frmFilenimbus.getContentPane().setFont(new Font("Arial", Font.PLAIN, 17));
		frmFilenimbus.setForeground(SystemColor.desktop);
		frmFilenimbus.setTitle("FileNimbus");
		frmFilenimbus.setBounds(100, 100, 450, 300);
		frmFilenimbus.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frmFilenimbus.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				try {
					MensajeInfo("FileNimbus", "We will miss you.");
					mainClient.logout();
					mainClient.close();
				}
				catch(Exception i){
					System.out.println("Something went wrong while closing the program.");
				}
			}
		});
		frmFilenimbus.getContentPane().setLayout(new CardLayout(0, 0));
		
		
		loginPanel = new JPanel();
		FlowLayout fl_loginPanel = (FlowLayout) loginPanel.getLayout();
		fl_loginPanel.setVgap(50);
		fl_loginPanel.setHgap(50);
		loginPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(loginPanel, "name_418755019206011");
		
		Box loginBox = Box.createVerticalBox();
		loginBox.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.setEnabled(false);
		loginBox.setBorder(null);
		loginPanel.add(loginBox);
		
		statLabel = new JLabel("Welcome");
		statLabel.setFont(new Font("Arial", Font.BOLD, 16));
		statLabel.setAlignmentX(0.5f);
		loginBox.add(statLabel);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		loginBox.add(verticalStrut);
		
		JLabel userLabel = new JLabel("User");
		userLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.add(userLabel);
		userLabel.setHorizontalAlignment(SwingConstants.LEFT);
		userLabel.setAlignmentX(0.5f);
		
		userField = new JTextField();
		userField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				if (key.getKeyCode() == KeyEvent.VK_ENTER) {
					login();
				}
			}
		});
		userField.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.add(userField);
		userField.setColumns(20);
		
		Component verticalStrut_2 = Box.createVerticalStrut(10);
		loginBox.add(verticalStrut_2);
		
		JLabel passLabel = new JLabel("Password");
		passLabel.setFont(new Font("Arial", Font.PLAIN, 15));
		loginBox.add(passLabel);
		passLabel.setHorizontalAlignment(SwingConstants.LEFT);
		passLabel.setAlignmentX(0.5f);
		
		passField = new JPasswordField();
		passField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				if (key.getKeyCode() == KeyEvent.VK_ENTER) {
					login();
				}
			}
		});
		passField.setFont(new Font("Arial", Font.PLAIN, 15));
		passField.setColumns(20);
		loginBox.add(passField);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		loginBox.add(verticalStrut_1);
		
		Box horizontalBox = Box.createHorizontalBox();
		loginBox.add(horizontalBox);
		
		JButton btnRegister = new JButton("Sign up");
		btnRegister.setFont(new Font("Arial", Font.PLAIN, 15));
		btnRegister.setMaximumSize(new Dimension(100, 25));
		btnRegister.setMinimumSize(new Dimension(100, 25));
		horizontalBox.add(btnRegister);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut);
		
		JButton btnLogin = new JButton("Login");
		
		btnLogin.setBorder(UIManager.getBorder("Button.border"));
		btnLogin.setMaximumSize(new Dimension(100, 25));
		btnLogin.setMinimumSize(new Dimension(100, 25));
		horizontalBox.add(btnLogin);
		btnLogin.setFont(new Font("Arial", Font.PLAIN, 15));
		btnLogin.setForeground(new Color(0, 0, 0));
		btnLogin.setBackground(UIManager.getColor("Button.background"));
		btnLogin.setAlignmentX(0.5f);
		
		signUpPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) signUpPanel.getLayout();
		flowLayout.setVgap(50);
		flowLayout.setHgap(50);
		signUpPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(signUpPanel, "name_418755085745115");
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setFont(new Font("Arial", Font.PLAIN, 15));
		verticalBox.setEnabled(false);
		verticalBox.setBorder(null);
		signUpPanel.add(verticalBox);
		
		JLabel label = new JLabel("Register new account");
		label.setFont(new Font("Arial", Font.BOLD, 16));
		label.setAlignmentX(0.5f);
		verticalBox.add(label);
		
		Component verticalStrut_3 = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut_3);
		
		JLabel label_1 = new JLabel("User");
		label_1.setHorizontalAlignment(SwingConstants.LEFT);
		label_1.setFont(new Font("Arial", Font.PLAIN, 15));
		label_1.setAlignmentX(0.5f);
		verticalBox.add(label_1);
		
		userRegister = new JTextField();
		userRegister.setFont(new Font("Arial", Font.PLAIN, 15));
		userRegister.setColumns(20);
		verticalBox.add(userRegister);
		
		Component verticalStrut_4 = Box.createVerticalStrut(10);
		verticalBox.add(verticalStrut_4);
		
		JLabel label_2 = new JLabel("Password");
		label_2.setHorizontalAlignment(SwingConstants.LEFT);
		label_2.setFont(new Font("Arial", Font.PLAIN, 15));
		label_2.setAlignmentX(0.5f);
		verticalBox.add(label_2);
		
		passRegister1 = new JPasswordField();
		passRegister1.setFont(new Font("Arial", Font.PLAIN, 15));
		passRegister1.setColumns(20);
		verticalBox.add(passRegister1);
		
		JLabel label_3 = new JLabel("Repeat password");
		label_3.setHorizontalAlignment(SwingConstants.LEFT);
		label_3.setFont(new Font("Arial", Font.PLAIN, 15));
		label_3.setAlignmentX(0.5f);
		verticalBox.add(label_3);
		
		passRegister2 = new JPasswordField();
		passRegister2.setFont(new Font("Arial", Font.PLAIN, 15));
		passRegister2.setColumns(20);
		verticalBox.add(passRegister2);
		
		Component verticalStrut_5 = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut_5);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		JButton backToLogin = new JButton("Back");
		
		backToLogin.setMinimumSize(new Dimension(100, 25));
		backToLogin.setMaximumSize(new Dimension(100, 25));
		backToLogin.setFont(new Font("Arial", Font.PLAIN, 15));
		horizontalBox_1.add(backToLogin);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalBox_1.add(horizontalStrut_1);
		
		JButton btnRegisterConfirm = new JButton("Confirm");
		btnRegisterConfirm.setMinimumSize(new Dimension(100, 25));
		btnRegisterConfirm.setMaximumSize(new Dimension(100, 25));
		btnRegisterConfirm.setForeground(Color.BLACK);
		btnRegisterConfirm.setFont(new Font("Arial", Font.PLAIN, 15));
		btnRegisterConfirm.setBorder(UIManager.getBorder("Button.border"));
		btnRegisterConfirm.setBackground(SystemColor.menu);
		btnRegisterConfirm.setAlignmentX(0.5f);
		horizontalBox_1.add(btnRegisterConfirm);
		
		userPanel = new JPanel();
		userPanel.setBackground(new Color(30, 144, 255));
		frmFilenimbus.getContentPane().add(userPanel, "name_418755151851020");
		
		JPanel panelMyFiles = new JPanel();
		panelMyFiles.setBackground(SystemColor.inactiveCaptionBorder);
		
		JLabel lblMyFiles = new JLabel("My Files");
		lblMyFiles.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JScrollPane scrollPane = new JScrollPane();
		
		// Boton update declarado
		JButton btnUpdateFiles = new JButton("Update");
		btnUpdateFiles.setForeground(Color.WHITE);
		btnUpdateFiles.setBackground(new Color(100, 149, 237));
		
 		btnUpdateFiles.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				// Rellenamos la tabla con los datos de los archivos
 				cargarDatosTabla();
 			}
 		});
		 		
		// Botones de seleccionar y deseleccionar
		JButton btnSelectAll = new JButton("Select all");
		btnSelectAll.setForeground(Color.WHITE);
		btnSelectAll.setBackground(new Color(100, 149, 237));
		
		JButton btnDeselectAll = new JButton("Deselect all");
		btnDeselectAll.setForeground(Color.WHITE);
		btnDeselectAll.setBackground(new Color(100, 149, 237));

		// Boton para descargar archivos
		JButton btnDownloadFile = new JButton("Download");
		btnDownloadFile.setForeground(Color.WHITE);
		btnDownloadFile.setBackground(new Color(100, 149, 237));
		
		btnDownloadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recorrerListaTabla(DOWNLOAD);
			}
		});
		
		// Boton para borrar archivos
		JButton btnDelete = new JButton("Delete");
		btnDelete.setForeground(new Color(255, 255, 255));
		btnDelete.setBackground(new Color(100, 149, 237));
		
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recorrerListaTabla(DELETE);
			}
		});
		
		// Boton para compartir
		JButton btnShare = new JButton("Share");
		btnShare.setBackground(new Color(100, 149, 237));
		btnShare.setForeground(new Color(255, 255, 255));
		GroupLayout gl_panelMyFiles = new GroupLayout(panelMyFiles);
		gl_panelMyFiles.setHorizontalGroup(
			gl_panelMyFiles.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelMyFiles.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelMyFiles.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
						.addGroup(gl_panelMyFiles.createSequentialGroup()
							.addComponent(lblMyFiles)
							.addGap(18)
							.addComponent(btnSelectAll)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDeselectAll)
							.addPreferredGap(ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
							.addComponent(btnDownloadFile)
							.addGap(18)
							.addComponent(btnShare)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnDelete)
							.addGap(18)
							.addComponent(btnUpdateFiles)
							.addContainerGap())))
		);
		gl_panelMyFiles.setVerticalGroup(
			gl_panelMyFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelMyFiles.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelMyFiles.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMyFiles)
						.addComponent(btnSelectAll)
						.addComponent(btnDeselectAll)
						.addComponent(btnUpdateFiles)
						.addComponent(btnDelete)
						.addComponent(btnShare)
						.addComponent(btnDownloadFile))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
					.addContainerGap())
		);
		panelMyFiles.setLayout(gl_panelMyFiles);
		
		btnShare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				recorrerListaTabla(SHARE);
			}
		});
		
		// ********************************************* Tabla ***********************************************************
		table=new JTable();
		scrollPane.setViewportView(table);
		
		// Rellenamos la tabla con los datos de los archivos
    	model = new DefaultTableModel()
		{
    		Class[] columnTypes = new Class[] {
				Boolean.class, String.class, String.class, String.class, String.class
			};
    		
    		public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		};
		model.addColumn("Select");
	    model.addColumn("Id");
	    model.addColumn("Name");
	    model.addColumn("Type");
	    model.addColumn("Propietary");
		table.setModel(model);
		asignarTamanyoColumnasTabla();
		
 		// Seleccionar todas las filas de la tabla
 		btnSelectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				marcarDesmarcarFilas(true);
			}
		});
		
 		// Deseleccionar todas las filas de la tabla
		btnDeselectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				marcarDesmarcarFilas(false);
			}
		});
		// ********************************************* Fin Tabla ***********************************************************
	    
		JPanel panelUploadFiles = new JPanel();
		panelUploadFiles.setBackground(SystemColor.inactiveCaptionBorder);
		
		JPanel panelUsuario = new JPanel();
		panelUsuario.setBackground(new Color(30, 144, 255));
		
		JPanel panelMenu = new JPanel();
		panelMenu.setBackground(new Color(30, 144, 255));
		
		GroupLayout gl_userPanel = new GroupLayout(userPanel);
		gl_userPanel.setHorizontalGroup(
			gl_userPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_userPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_userPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(panelMyFiles, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
						.addGroup(gl_userPanel.createSequentialGroup()
							.addComponent(panelUsuario, GroupLayout.PREFERRED_SIZE, 270, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 241, Short.MAX_VALUE)
							.addComponent(panelMenu, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(panelUploadFiles, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_userPanel.setVerticalGroup(
			gl_userPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_userPanel.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_userPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(panelUsuario, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
						.addComponent(panelMenu, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panelUploadFiles, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panelMyFiles, GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);
		menuBar.setBackground(new Color(30, 144, 255));
		panelMenu.add(menuBar);
		
		JMenu menu = new JMenu("");
		menu.setIcon(new ImageIcon(LoginWindow.class.getResource("/main/resources/menuIconMini.png")));
		menu.setBackground(new Color(30, 144, 255));
		menuBar.add(menu);
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mntmSettings.setHorizontalAlignment(SwingConstants.LEFT);
		mntmSettings.setBackground(new Color(135, 206, 235));
		mntmSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				userPanel.setVisible(false);
				panelSettings.setVisible(true);
				lblOldname.setText(mainClient.getUserName());
			}
		});
		menu.add(mntmSettings);
		
		JMenuItem mntmLogout = new JMenuItem("Logout");
		mntmLogout.setHorizontalAlignment(SwingConstants.LEFT);
		mntmLogout.setBackground(new Color(135, 206, 235));
		mntmLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try{
					mainClient.logout();
				}
				catch(Exception o){
					System.out.println(o.getMessage());
				}
				loginPanel.setVisible(true);
				userPanel.setVisible(false);
			}
		});
		menu.add(mntmLogout);
		
		lblUser = new JLabel("User");
		lblUser.setForeground(Color.WHITE);
		lblUser.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		JPanel ImgPanel = new JPanel();
		ImgPanel.setBackground(new Color(30, 144, 255));
		
		JLabel lblWelcome = new JLabel("Welcome");
		lblWelcome.setForeground(Color.WHITE);
		lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 11));
		GroupLayout gl_panelUsuario = new GroupLayout(panelUsuario);
		gl_panelUsuario.setHorizontalGroup(
			gl_panelUsuario.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUsuario.createSequentialGroup()
					.addComponent(ImgPanel, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblWelcome)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblUser)
					.addContainerGap(135, Short.MAX_VALUE))
		);
		gl_panelUsuario.setVerticalGroup(
			gl_panelUsuario.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUsuario.createSequentialGroup()
					.addGroup(gl_panelUsuario.createParallelGroup(Alignment.LEADING)
						.addComponent(ImgPanel, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panelUsuario.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panelUsuario.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblWelcome)
								.addComponent(lblUser))))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		ImgPanel.setLayout(null);
		
		Imagen img = new Imagen();
		img.setBounds(0, 0, 40, 40);
		ImgPanel.add(img);
		panelUsuario.setLayout(gl_panelUsuario);
		
		JLabel fileName = new JLabel(NO_FILE_SELECTED);
		
		JButton btnSelectFile = new JButton("Select file");
		btnSelectFile.setForeground(Color.WHITE);
		btnSelectFile.setBackground(new Color(100, 149, 237));
		
		JButton btnUploadFile = new JButton("Upload file");
		btnUploadFile.setForeground(Color.WHITE);
		btnUploadFile.setBackground(new Color(100, 149, 237));
		
		JLabel lblNewFile = new JLabel("New File");
		lblNewFile.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout gl_panelUploadFiles = new GroupLayout(panelUploadFiles);
		gl_panelUploadFiles.setHorizontalGroup(
			gl_panelUploadFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUploadFiles.createSequentialGroup()
					.addGroup(gl_panelUploadFiles.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelUploadFiles.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblNewFile)
							.addPreferredGap(ComponentPlacement.RELATED, 435, Short.MAX_VALUE)
							.addComponent(btnSelectFile)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnUploadFile))
						.addGroup(gl_panelUploadFiles.createSequentialGroup()
							.addGap(22)
							.addComponent(fileName)))
					.addContainerGap())
		);
		gl_panelUploadFiles.setVerticalGroup(
			gl_panelUploadFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelUploadFiles.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_panelUploadFiles.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewFile)
						.addComponent(btnUploadFile)
						.addComponent(btnSelectFile))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(fileName)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panelUploadFiles.setLayout(gl_panelUploadFiles);
		
		// File upload button
		btnUploadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (chosenFile == null) {
						throw new Excepciones("File not selected");
					}
					
					if (chosenFile.exists()) {
						try {
							mainClient.upload(chosenFile);
							fileName.setText(NO_FILE_SELECTED);
							chosenFile = null;
							
							// Actualizamos la tabla de archivos
							cargarDatosTabla();
						}
						catch(Exception u) {
							System.out.println("File could not be uploaded");
						}
					}
					else {
						throw new Excepciones("File not exist");
					}
			} catch(Excepciones ex) {
				MensajeError(ex.exErrorPersonalizado());
				return;
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				return;
			}
		}
	});
		
		// File select button
		btnSelectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					// Get the file
					chosenFile = fileChooser.getSelectedFile();
					fileName.setText(chosenFile.getAbsolutePath());
				}
			}
		});
		userPanel.setLayout(gl_userPanel);

		// Register button
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				signUpPanel.setVisible(true);
				loginPanel.setVisible(false);
				//frmFilenimbus.setContentPane(signUpPanel);
				limpiarCamposLogin();
			}
		});

		// Login button
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				login();
			}
		});

		// Back to login button
		backToLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				signUpPanel.setVisible(false);
				loginPanel.setVisible(true);
				//frmFilenimbus.setContentPane(loginPanel);
				limpiarCamposSingIn();
			}
		});
		
		
		// Confirm new signup button
		btnRegisterConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String userName = userRegister.getText();
					String pwd1 = new String(passRegister1.getPassword());
					String pwd2 = new String(passRegister2.getPassword());
	
					if (userName.equals("") || pwd1.equals("") || pwd2.equals("")) {
						throw new Excepciones("Some field is empty");
					}
					
					// Comprobar longitudes de los campos
					comprobarLongUser(userName);
					comprobarLongPass(pwd1);
					
					if (pwd1.equals(pwd2)){
						// Call the register method
						try {
							if(mainClient.signUp(userName, pwd1)){
								statLabel.setText("Account created succesfully.");
								signUpPanel.setVisible(false);
								loginPanel.setVisible(true);
								limpiarCamposSingIn();
							}
						}
						catch (Excepciones ex) {
							MensajeError(ex.exErrorPersonalizado());
						}
						catch (Exception a) {
							throw new Excepciones("Error when trying to sign up.");
						}
					}
					else {
						throw new Excepciones("Passwords doesn't match");
					}
				} catch(Excepciones ex) {
					MensajeError(ex.exErrorPersonalizado());
					return;
				}
			}
		});
		
		// Checks the server connection
		try {
			mainClient.initializeClient();
			statLabel.setText("Server connected.");
			
			panelSettings = new JPanel();
			panelSettings.setBackground(new Color(30, 144, 255));
			frmFilenimbus.getContentPane().add(panelSettings, "name_417711051764357");
			
			JLabel lblSettings = new JLabel("Settings");
			lblSettings.setFont(new Font("Tahoma", Font.BOLD, 16));
			
			JPanel panel_ch_user = new JPanel();
			
			JPanel panel_ch_pass = new JPanel();
			
			JButton btnBack = new JButton("Back");
			btnBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					limpiarCamposSettings();
					userPanel.setVisible(true);
					panelSettings.setVisible(false);
					lblUser.setText(mainClient.getUserName());
				}
			});
			GroupLayout gl_panelSettings = new GroupLayout(panelSettings);
			gl_panelSettings.setHorizontalGroup(
				gl_panelSettings.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panelSettings.createSequentialGroup()
						.addGap(18)
						.addGroup(gl_panelSettings.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panelSettings.createSequentialGroup()
								.addComponent(btnBack)
								.addContainerGap())
							.addGroup(gl_panelSettings.createSequentialGroup()
								.addGroup(gl_panelSettings.createParallelGroup(Alignment.LEADING)
									.addComponent(lblSettings)
									.addComponent(panel_ch_user, GroupLayout.PREFERRED_SIZE, 667, GroupLayout.PREFERRED_SIZE)
									.addComponent(panel_ch_pass, GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
								.addContainerGap(19, GroupLayout.PREFERRED_SIZE))))
			);
			gl_panelSettings.setVerticalGroup(
				gl_panelSettings.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panelSettings.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblSettings)
						.addGap(18)
						.addComponent(panel_ch_user, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(panel_ch_pass, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
						.addComponent(btnBack)
						.addContainerGap())
			);
			
			JLabel lblChangePassword = new JLabel("Change password");
			lblChangePassword.setFont(new Font("Tahoma", Font.BOLD, 14));
			
			passNewpassword = new JPasswordField();
			passNewpassword.setColumns(10);
			
			JButton btnChange_password = new JButton("Change");
			btnChange_password.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					changePassword();
				}
			});
			btnChange_password.setBackground(new Color(100, 149, 237));
			btnChange_password.setForeground(new Color(255, 255, 255));
			
			passRepeatpassword = new JPasswordField();
			passRepeatpassword.setColumns(10);
			
			JLabel lblPassword = new JLabel("Password:");
			
			JLabel lblRepeatPassword = new JLabel("Repeat password:");
			GroupLayout gl_panel_ch_pass = new GroupLayout(panel_ch_pass);
			gl_panel_ch_pass.setHorizontalGroup(
				gl_panel_ch_pass.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_ch_pass.createSequentialGroup()
						.addContainerGap(588, Short.MAX_VALUE)
						.addComponent(btnChange_password)
						.addContainerGap())
					.addGroup(gl_panel_ch_pass.createSequentialGroup()
						.addGap(32)
						.addGroup(gl_panel_ch_pass.createParallelGroup(Alignment.LEADING)
							.addComponent(lblChangePassword)
							.addGroup(gl_panel_ch_pass.createSequentialGroup()
								.addGroup(gl_panel_ch_pass.createParallelGroup(Alignment.TRAILING)
									.addComponent(lblPassword)
									.addComponent(lblRepeatPassword))
								.addGap(18)
								.addGroup(gl_panel_ch_pass.createParallelGroup(Alignment.LEADING, false)
									.addComponent(passRepeatpassword)
									.addComponent(passNewpassword, GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))))
						.addContainerGap(188, Short.MAX_VALUE))
			);
			gl_panel_ch_pass.setVerticalGroup(
				gl_panel_ch_pass.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_ch_pass.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblChangePassword)
						.addGap(18)
						.addGroup(gl_panel_ch_pass.createParallelGroup(Alignment.BASELINE)
							.addComponent(passNewpassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblPassword))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panel_ch_pass.createParallelGroup(Alignment.BASELINE)
							.addComponent(passRepeatpassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblRepeatPassword))
						.addPreferredGap(ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
						.addComponent(btnChange_password)
						.addContainerGap())
			);
			panel_ch_pass.setLayout(gl_panel_ch_pass);
			
			JLabel lblChangeUserName = new JLabel("Change user name");
			lblChangeUserName.setFont(new Font("Tahoma", Font.BOLD, 14));
			
			txtNewuser = new JTextField();
			txtNewuser.setColumns(10);
			
			JButton btnChange_user = new JButton("Change");
			btnChange_user.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					changeUser();
				}
			});
			btnChange_user.setForeground(new Color(255, 255, 255));
			btnChange_user.setBackground(new Color(100, 149, 237));
			
			JLabel lblName = new JLabel("Name:");
			
			lblOldname = new JLabel(mainClient.getUserName());
			
			JLabel lblNewName = new JLabel("New name:");
			GroupLayout gl_panel_ch_user = new GroupLayout(panel_ch_user);
			gl_panel_ch_user.setHorizontalGroup(
				gl_panel_ch_user.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_ch_user.createSequentialGroup()
						.addContainerGap(588, Short.MAX_VALUE)
						.addComponent(btnChange_user)
						.addContainerGap())
					.addGroup(gl_panel_ch_user.createSequentialGroup()
						.addGap(42)
						.addComponent(lblChangeUserName)
						.addContainerGap(496, Short.MAX_VALUE))
					.addGroup(gl_panel_ch_user.createSequentialGroup()
						.addGap(67)
						.addGroup(gl_panel_ch_user.createParallelGroup(Alignment.TRAILING)
							.addComponent(lblName)
							.addComponent(lblNewName))
						.addGap(18)
						.addGroup(gl_panel_ch_user.createParallelGroup(Alignment.LEADING)
							.addComponent(txtNewuser, GroupLayout.PREFERRED_SIZE, 339, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblOldname))
						.addContainerGap(189, Short.MAX_VALUE))
			);
			gl_panel_ch_user.setVerticalGroup(
				gl_panel_ch_user.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel_ch_user.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblChangeUserName)
						.addGap(18)
						.addGroup(gl_panel_ch_user.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblName)
							.addComponent(lblOldname))
						.addGap(14)
						.addGroup(gl_panel_ch_user.createParallelGroup(Alignment.BASELINE)
							.addComponent(txtNewuser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblNewName))
						.addContainerGap(48, Short.MAX_VALUE))
					.addGroup(gl_panel_ch_user.createSequentialGroup()
						.addContainerGap(108, Short.MAX_VALUE)
						.addComponent(btnChange_user)
						.addContainerGap())
			);
			panel_ch_user.setLayout(gl_panel_ch_user);
			panelSettings.setLayout(gl_panelSettings);
		}
		catch (Excepciones ex) {
			statLabel.setText(ex.exErrorPersonalizado());
			btnLogin.setEnabled(false);
			btnRegister.setEnabled(false);
		}
		catch(Exception e){
			statLabel.setText("Server disconnected.");
			btnLogin.setEnabled(false);
			btnRegister.setEnabled(false);
		}
	}
	
	// Comprueba la longitud del campo User name
	protected void comprobarLongUser(String userName) throws Excepciones {
		if (!comprobarLongitud(userName, 6, 8)) {
			throw new Excepciones("User: min lenght: 6 and max lenght 8");
		}
	}
	
	// Comprueba la longitud de la contrasenya
	protected void comprobarLongPass(String pass) throws Excepciones {
		if (!comprobarLongitud(pass, 6, 8)) {
			throw new Excepciones("Pass: min lenght: 6 and max lenght 8");
		}
	}

	// Comprueba que la longitud este dentro de un rango
	protected boolean comprobarLongitud(String texto, int min, int max) {
		if ((texto.length() >= min) && (texto.length() <= max)) {
			return true;
		}
		return false;
	}
	
	protected void limpiarCamposLogin() {
		 userField.setText("");
		 passField.setText("");
	}
	
	protected void limpiarCamposSingIn() {
		userRegister.setText("");
		passRegister1.setText("");
		passRegister2.setText("");
	}
	
	protected void limpiarCamposSettings() {
		lblOldname.setText("");
		txtNewuser.setText("");
		passNewpassword.setText("");
		passRepeatpassword.setText("");
	}
	
	protected void changeUser() {
		try {
			String newName = txtNewuser.getText();
			
			// Campo vacio
			if (newName.equals("")) {
				throw new Excepciones("Empty user name");
			}
			
			// Comprobar longitud del campo
			comprobarLongUser(newName);
			
			if (comprobarPassword()) {
				// Cambiar nombre de usuario
				if (mainClient.cambiarUser(newName)) {
					MensajeInfo("Name changed successfully!");
					limpiarCamposSettings();
					lblOldname.setText(newName);
				}
			}
		} catch(Excepciones ex) {
			MensajeError(ex.exErrorPersonalizado());
			return;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}

	protected void changePassword() {
		try {
			String newPWD = new String(passNewpassword.getPassword());
			String rePWD = new String(passRepeatpassword.getPassword());
			
			// Campos vacios
			if (newPWD.equals("")) {
				throw new Excepciones("Empty password");
			} else if (rePWD.equals("")) {
				throw new Excepciones("Empty repeat password");
			}
			
			// Comprobar longitud del campo
			comprobarLongPass(newPWD);
			
			// Campos iguales
			if (newPWD.equals(rePWD)) {
				if (comprobarPassword()) {
					if (mainClient.cambiarPassword(newPWD)) {
						MensajeInfo("Password changed successfully!");
						limpiarCamposSettings();
						lblOldname.setText(mainClient.getUserName());
					}
				}
			} else {
				throw new Excepciones("Passwords don't match");
			}
		} catch(Excepciones ex) {
			MensajeError(ex.exErrorPersonalizado());
			return;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
	}
	
	protected boolean comprobarPassword() {
		try {
			JPasswordField pwd = new JPasswordField();
			int action = JOptionPane.showConfirmDialog(panelSettings, pwd,
					"Enter password:", JOptionPane.OK_CANCEL_OPTION);
			
			String paswd = new String(pwd.getPassword());
			
			// 0 para Aceptar, 1 para No, 2 para Cancelar y -1 para el cierre de la ventana. 
			if(action == 0) {
				if ( (!paswd.equals(null)) && (!paswd.equals("")) ) {
					if (mainClient.comprobarUserPassword(paswd)) {
						return true;
					}
				} else {
					throw new Excepciones("Empty password");
				}
			}
			return false;
			
		} catch(Excepciones ex) {
			MensajeError(ex.exErrorPersonalizado());
			return false;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	

	protected void recorrerListaTabla(byte accion) {
		ArrayList<String> idFicheros = new ArrayList<String>();
		int total = 0;
		
		// Pillar todas las filas seleccionadas
		for(int fila=0; fila<table.getRowCount(); fila++) {
			
			// Si esta seleccionado, lo guardamos en el array
			if(Boolean.valueOf(table.getValueAt(fila, 0).toString())) {
        		total++;
        		idFicheros.add(table.getValueAt(fila, 1).toString());
        	}
        }

		if (total > 0) {
			switch (accion) {
				case DELETE:
					int opcion = JOptionPane.showConfirmDialog(userPanel, "Are you sure to delete?", 
						"Delete files", JOptionPane.OK_CANCEL_OPTION);
					
					if (opcion == JOptionPane.OK_OPTION) {
			             // Recorrer la lista y llamar uno a uno a delete
						for (int id=0; id<idFicheros.size(); id++) {
							try {
								mainClient.delete(Integer.parseInt(idFicheros.get(id)));
							} catch (Exception e1) {
								System.out.println(e1.getMessage());
							}
						}
						
						// Recargamos la tabla
						cargarDatosTabla();
			        }
					break;
					
				case DOWNLOAD:
					JFileChooser directoryChooser = new JFileChooser();
					directoryChooser.setDialogTitle("Save directory");
					directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					directoryChooser.setAcceptAllFileFilterUsed(false);
					
					if(directoryChooser.showOpenDialog(userPanel) == JFileChooser.APPROVE_OPTION){
						
						// Directorio donde guardar los ficheros
						String directorio = directoryChooser.getSelectedFile().getPath();
						
						// TODO comprobar que funcione de verdad
						if (directoryChooser.getSelectedFile().canWrite()) {
							System.out.println("Puedo acceder y escribir en el directorio");
							
							for (int id=0; id<idFicheros.size(); id++) {
								try {
									mainClient.download(Integer.parseInt(idFicheros.get(id)), directorio);
								} catch (Exception e) {
									System.out.println(e.getMessage());
								}
							}
						} else {
							System.out.println("No puedo acceder ni escribir en el directorio");
						}
					}
					break;
					
				case SHARE:
					boolean shared = false;
					JTextField shareUserTF = new JTextField();
					int action = JOptionPane.showConfirmDialog(userPanel, shareUserTF,
							"Enter the username to share:", JOptionPane.OK_CANCEL_OPTION);
					
					// 0 para Aceptar, 1 para No, 2 para Cancelar y -1 para el cierre de la ventana. 
					if(action == 0) {
						String shareUser = shareUserTF.getText();
						if ( (!shareUser.equals(null)) && (!shareUser.equals("")) ) {
							// Recorrer la lista y llamar uno a uno a share
							try {
								for (int id=0; id<idFicheros.size(); id++) {
								
									if (mainClient.share(shareUser, Integer.parseInt(idFicheros.get(id)))) {
										shared = true;
									}
								}
							} catch (Excepciones ex) {
								MensajeError(ex.exErrorPersonalizado());
							} catch (Exception e1) {
								System.out.println(e1.getMessage());
							}
							if (shared) {
								MensajeInfo("Shared!");
							}
						} else {
							MensajeError("Empty username");
						}
					}
					break;
					
				default:
					System.out.println("Action not contempled");
					break;
			}
		} else {
			JOptionPane.showMessageDialog(frmFilenimbus, "Nothing is selected!", 
					"Table files", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void cargarDatosTabla() {
		ArrayList<Archivo> lista;
		try {
			lista = mainClient.check();
			if(lista!=null) {
				
				// Borramos los datos anteriores si hay
		    	if (model.getRowCount() > 0) {
		    		for (int fila=model.getRowCount()-1; fila>=0; fila--) {
		    			model.removeRow(fila);
		    		}
		    	}
		    	
				for(int num=0; num<lista.size(); num++) {
					model.addRow(new Object[num]);
					model.setValueAt(false,num,0);
			        model.setValueAt(lista.get(num).getId(), num, 1);
			        model.setValueAt(lista.get(num).getNombre(), num, 2);
			        model.setValueAt(lista.get(num).getTipo(), num, 3);
			        model.setValueAt(lista.get(num).getShared(), num, 4);
				}
			}
		} catch (Exception e) {
			System.out.println("Error in file list: "+e.getMessage());
		}
	}
	
	private void asignarTamanyoColumnasTabla() {
		
		table.getColumnModel().getColumn(0).setPreferredWidth(45);
		table.getColumnModel().getColumn(0).setMinWidth(45);
		table.getColumnModel().getColumn(0).setMaxWidth(45);
		table.getColumnModel().getColumn(1).setPreferredWidth(35);
		table.getColumnModel().getColumn(1).setMinWidth(35);
		table.getColumnModel().getColumn(1).setMaxWidth(35);
		table.getColumnModel().getColumn(3).setPreferredWidth(70);
		table.getColumnModel().getColumn(3).setMinWidth(70);
		table.getColumnModel().getColumn(3).setMaxWidth(70);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.getColumnModel().getColumn(4).setMinWidth(100);
		table.getColumnModel().getColumn(4).setMaxWidth(100);
	}
	
	// Para marcar o desmarcar todas las filas de la tabla
	public void marcarDesmarcarFilas(boolean valor) {
    	
    	for(int fila=0; fila<table.getRowCount(); fila++) {
			if(Boolean.valueOf(table.getValueAt(fila, 0).toString()) == !valor) {
        		table.setValueAt(valor, fila, 0);
        	}
        }
    }
	
	// Get de frmFilenimbus que se llama desde main
	public JFrame getFrmFilenimbus() {
		return frmFilenimbus;
	}
	
	// Para tener alertas de informacion general con el mismo formato
    private void MensajeInfo(String mensaje) {
    	MensajeInfo("Info", mensaje);
    }
    private void MensajeInfo(String titulo, String mensaje) {
    	JOptionPane.showMessageDialog(frmFilenimbus, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
	}
    
 // Para tener una alerta de errores general con el mismo formato
    private void MensajeError(String mensaje) {
    	JOptionPane.showMessageDialog(frmFilenimbus, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
	}
    
    private void login() {
    	try {
    		String userName = userField.getText();
			String pwd = new String(passField.getPassword());
			
			// Comprobar campos vacios
			if (userName.equals("")) {
				throw new Excepciones("Empty user");
			} else if (pwd.equals("")) {
				throw new Excepciones("Empty password");
			}
			
			// Call connect method
			if(mainClient.login(userName, pwd)){
				statLabel.setText("Conectado.");
				loginPanel.setVisible(false);
				userPanel.setVisible(true);
				//frmFilenimbus.setContentPane(userPanel);
				
				// Cargar los archivos del usuario en la tabla
				cargarDatosTabla();
				lblUser.setText(userName);
				limpiarCamposLogin();
			}
		}
		catch(Excepciones ex) {
			MensajeError(ex.exErrorPersonalizado());
			return;
		}
		catch(Exception ex) {
			statLabel.setText(ex.getMessage());
			return;
		}
		finally{
			//statLabel.setText(tmp);
		}
    }
}

package Client.Gui;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.Choice;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import Client.Client;
//import Client.ClientCallbackImpl;
import Client.ResponseException;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class HomeForm
{

	JFrame frame;
	private JTextField textField;
	private JTextField textField2;
	private JTextField textField3;
	final JFileChooser fc = new JFileChooser();
	private boolean edit = false;
	private int selection;
	private ChatForm chat;
	private String adress;
	
	public JPanel shareDocument()
	{
		JPanel panel = new JPanel(new GridLayout(2,2));
		JLabel lblNewLabel = new JLabel("Nome File");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNewLabel);
		textField2 = new JTextField();
		panel.add(textField2);
		textField2.setColumns(10);
		
		JLabel lblNewLabel2 = new JLabel("Nome utente");
		lblNewLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNewLabel2);
		textField3 = new JTextField();
		panel.add(textField3);
		textField3.setColumns(10);
		return panel;
	}
	
	public HomeForm(String user, Client client, JFrame frame2) 
	{
		frame = new JFrame(user+" - Turing ");
		frame.setBounds(frame2.getX(), frame2.getY(), 516, 312);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnLogout = new JButton("Logout");
		JButton btnShowSection = new JButton("Show Section");
		JButton btnCreateDoc = new JButton("Create Document");
		JButton btnShareDocument = new JButton("Share Document");
		JButton btnListDocument = new JButton("List Document");
		JButton btnEditDocument = new JButton("Edit Document");
		
		JTextArea listDocument = new JTextArea();
		JScrollPane sp = new JScrollPane(listDocument);
		
		listDocument.setEditable(false);
		sp.setBounds(270, 20, 200, 180);
		frame.getContentPane().add(sp);
		
		btnCreateDoc.setBounds(10, 35, 135, 23);
		frame.getContentPane().add(btnCreateDoc);
		
		btnShowSection.setBounds(10, 95, 135, 23);
		frame.getContentPane().add(btnShowSection);
		
		btnLogout.setBounds(377, 225, 97 , 23);
		frame.getContentPane().add(btnLogout);
		
		btnEditDocument.setBounds(10, 65, 135, 23);
		frame.getContentPane().add(btnEditDocument);
		
		
		btnShareDocument.setBounds(10, 155, 135, 23);
		frame.getContentPane().add(btnShareDocument);	
		
		btnListDocument.setBounds(10, 185, 135, 23);
		frame.getContentPane().add(btnListDocument);
		
		JButton btnShowDocument = new JButton("Show Document");
		btnShowDocument.setBounds(10, 125, 135, 23);
		frame.getContentPane().add(btnShowDocument);
		
		
		final Choice choice = new Choice();
		choice.setBounds(170, 60, 90, 20);
		for(int i=0;i<=100;i++)
		{
			if(i==0)
				choice.add("Section - ");
			else
				choice.add("Section - "+i);
		}
		
		JPanel panel = new JPanel(new GridLayout(2,2));
		JLabel lblNewLabel = new JLabel("Nome File");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNewLabel);
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);
		JLabel lblNumeroSezioni = new JLabel("Numero Sezioni");
		lblNumeroSezioni.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNumeroSezioni);
		panel.add(choice);
		
		
		/**
		 * Creo documento
		 */
		btnCreateDoc.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				
				int selection=JOptionPane.showConfirmDialog(frame, panel, "Crea documento", JOptionPane.OK_CANCEL_OPTION);
				if(selection == JOptionPane.OK_OPTION)
				{
					if(choice.getSelectedIndex()==0 || textField .getText().isEmpty())
						JOptionPane.showMessageDialog(frame, "Dati non validi");
					else
					{
						
						try 
						{
							client.createDocument(textField .getText(), choice.getSelectedIndex());
							JOptionPane.showMessageDialog(frame, "Operazione, avvenuta con successo");
						} 
						catch (ResponseException e1) 
						{
							JOptionPane.showMessageDialog(frame, "Esiste gia' un docuemento con lo stesso nome");
						}
						catch(IOException e1)
						{
							JOptionPane.showMessageDialog(frame, e1.getMessage());
						}
					}
				}
				textField.setText("");
				choice.select(0);
			}
		});
		
		/**
		 * Aggiunge un collaboratore
		 */
		btnShareDocument.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				
				selection = JOptionPane.showConfirmDialog(frame, shareDocument(), "Aggiungi collaboratore", JOptionPane.OK_CANCEL_OPTION);
				if(selection == JOptionPane.OK_OPTION)
				{
					if(textField3.getText().isEmpty() || textField2.getText().isEmpty())
					{
						JOptionPane.showMessageDialog(frame,"Dati non validi");
					}
					else
					{
						try 
						{
							client.shareDocument(textField3.getText(), textField2.getText());
							JOptionPane.showMessageDialog(frame, "Operazione, avvenuta con successo");
						} 
						catch (ResponseException e1) 
						{
							JOptionPane.showMessageDialog(frame, e1.getMessage());
						}
						catch(IOException e1)
						{
							JOptionPane.showMessageDialog(frame, e1.getMessage());
						}
					}
				}
			}
		});
		
		
		
		/**
		 * Edit di un documento
		 */
		btnEditDocument.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				if(!edit)
				{
					try
					{
						selection=JOptionPane.showConfirmDialog(frame, panel, "Edit documento", JOptionPane.OK_CANCEL_OPTION);
						if(selection == JOptionPane.OK_OPTION)
						{
						
							if(choice.getSelectedIndex()==0 || textField .getText().isEmpty())
							{
								JOptionPane.showMessageDialog(frame, "Dati non validi");
							}
							else
							{
								
								adress= client.startEditDocument(textField .getText(), choice.getSelectedIndex());
								
								
								
								chat= new ChatForm(client.getUsername(), adress);
								chat.frame.setVisible(true);
								btnLogout.setEnabled(false);
								btnShowSection.setEnabled(false);
								btnCreateDoc.setEnabled(false);
								btnShareDocument.setEnabled(false);
								btnListDocument.setEnabled(false);
								btnShowDocument.setEnabled(false);
								btnEditDocument.setText("End edit");
								edit = true;
							}
						}	
						
					}
					catch(ResponseException e1)
					{
						JOptionPane.showMessageDialog(frame,e1.getLocalizedMessage());
					}
					catch(IOException e1)
					{
						JOptionPane.showMessageDialog(frame, e1.getMessage());
					}
					finally
					{
						textField.setText("");
						choice.select(0);
					}
				}
				else
				{
					try
					{
						chat.frame.setVisible(false);
						chat.interruptThread();
						
						client.endEditDocument();
						JOptionPane.showMessageDialog(frame, "Operazione, avvenuta con successo");
						btnEditDocument.setText("Edit Document");
						btnLogout.setEnabled(true);
						btnShowSection.setEnabled(true);
						btnCreateDoc.setEnabled(true);
						btnShareDocument.setEnabled(true);
						btnListDocument.setEnabled(true);
						btnShowDocument.setEnabled(true);
						edit = false;
					}
					catch(IOException e1)
					{
						JOptionPane.showMessageDialog(frame, e1.getMessage());
					}
					
				}
			}
		});
		
		/**
		 * Mostra lista di documenti
		 */
		btnListDocument.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					listDocument.setText(client.listDocument());
					JOptionPane.showMessageDialog(frame, "Operazione, avvenuta con successo");
				}
				catch(IOException e1)
				{
					JOptionPane.showMessageDialog(frame, e1.getMessage());
				}
			}
		});
		

		/**
		 * Mostra sezione di un documento
		 */
		btnShowSection.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					selection=JOptionPane.showConfirmDialog(frame, panel, "Show Section", JOptionPane.OK_CANCEL_OPTION);
					if(selection == JOptionPane.OK_OPTION)
					{
				
						if(choice.getSelectedIndex()==0 || textField .getText().isEmpty())
						{
							JOptionPane.showMessageDialog(frame, "Dati non validi");
						}
						else
						{
							JOptionPane.showMessageDialog(frame, client.showSection(textField .getText(), choice.getSelectedIndex()));
						}
					}	
				}
				catch(ResponseException e1)
				{
					JOptionPane.showMessageDialog(frame,e1.getLocalizedMessage());
				}
				catch(IOException e1)
				{
					JOptionPane.showMessageDialog(frame, e1.getMessage());
				}
				finally
				{
					textField.setText("");
					choice.select(0);
				}
			}
		});
		
		/**
		 * Mostra documento
		 */
		btnShowDocument.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				String input = JOptionPane.showInputDialog("Nome documento");
				if((input!=null))
				{
					if(input.length()==0)
						JOptionPane.showMessageDialog(frame,"Nome documento non puo' essere vuoto");
					else
					{
						try 
						{
							JOptionPane.showMessageDialog(frame,client.showDocument(input));
						} 
						catch(ResponseException e1)
						{
							JOptionPane.showMessageDialog(frame,e1.getLocalizedMessage());
						}
						catch(IOException e1)
						{
							JOptionPane.showMessageDialog(frame, e1.getMessage());
						}
					}
						
				}
			}
		});
		btnLogout.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					
					
					client.logout();
					client.getConnection().close();
					frame.setVisible(false);
					frame2.setVisible(true);
				}
				catch(ResponseException e1)
				{
					JOptionPane.showMessageDialog(frame,e1.getLocalizedMessage());
				}
				catch(IOException e1)
				{
					JOptionPane.showMessageDialog(frame, e1.getMessage());
				}
			}
		});
		
	}
}

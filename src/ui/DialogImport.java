package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DialogImport extends JDialog {
	JCheckBox lblSelectDemographicelectionResult = new JCheckBox("Select demographic / election result columns");
	JCheckBox lblLoadPopulationFrom = new JCheckBox("Load population from");
	JComboBox comboBoxFilePopulationColumn = new JComboBox();
	JComboBox comboBoxMapLayer = new JComboBox();
	JComboBox comboBoxFileLinkColumn = new JComboBox();
	public DialogImport() {
		setTitle("Import data");
		setModal(true);
		getContentPane().setLayout(null);
		
		comboBoxMapLayer.setBounds(10, 33, 137, 20);
		getContentPane().add(comboBoxMapLayer);
		
		comboBoxFileLinkColumn.setBounds(10, 87, 137, 20);
		getContentPane().add(comboBoxFileLinkColumn);
		
		comboBoxFilePopulationColumn.setBounds(10, 139, 137, 20);
		getContentPane().add(comboBoxFilePopulationColumn);
		
		JLabel lblLinkMapLayer = new JLabel("Link map layer");
		lblLinkMapLayer.setBounds(10, 11, 89, 14);
		getContentPane().add(lblLinkMapLayer);
		
		JLabel lblToColumn = new JLabel("To column");
		lblToColumn.setBounds(10, 64, 89, 14);
		getContentPane().add(lblToColumn);
		lblLoadPopulationFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				comboBoxFilePopulationColumn.setEnabled(lblLoadPopulationFrom.isSelected());
			}
		});
		
		lblLoadPopulationFrom.setBounds(10, 118, 137, 14);
		getContentPane().add(lblLoadPopulationFrom);
		
		JLabel lblNonmatchesMap = new JLabel("0 non-matches");
		lblNonmatchesMap.setBounds(157, 36, 89, 14);
		getContentPane().add(lblNonmatchesMap);
		
		JLabel lblNonmatchesFile = new JLabel("0 non-matches");
		lblNonmatchesFile.setBounds(157, 90, 89, 14);
		getContentPane().add(lblNonmatchesFile);
		lblSelectDemographicelectionResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		lblSelectDemographicelectionResult.setBounds(257, 11, 238, 14);
		getContentPane().add(lblSelectDemographicelectionResult);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("New check box");
		chckbxNewCheckBox.setBounds(267, 32, 184, 23);
		getContentPane().add(chckbxNewCheckBox);
		
		JCheckBox checkBox = new JCheckBox("New check box");
		checkBox.setBounds(267, 60, 184, 23);
		getContentPane().add(checkBox);
		
		JButton btnOk = new JButton("OK");
		btnOk.setBounds(123, 435, 89, 23);
		getContentPane().add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(257, 435, 89, 23);
		getContentPane().add(btnCancel);
	}
}

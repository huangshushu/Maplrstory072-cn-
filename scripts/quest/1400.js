/* Author: Xterminator (Modified by RMZero213)
	NPC Name: 		Roger
	Map(s): 		Maple Road : Lower level of the Training Camp (2)
	Description: 		Quest - Roger's Apple
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
	qm.dispose();
    } else {
	if (mode == 1) {
	    status++;
	} else {
	    status--;
	}
	if (status == 0) {
	    qm.sendNext("Hmm, you're making good progress with your leveling. Have you decided on which job you want to take? You could be a Warrior with great strength and high HP, a Magician with many spells, a Bowman that shoots arrows from afar, a Thief that uses quick, sneaky attacks, or a Pirate with all kinds of flashy chain skills... There are so many!");
	} else if (status == 1) {
	    qm.sendSimple("If you go to Victoria Island, you can advance to the job of your choice by going to the right Job Instructor. But before that, lemme know which one you're interested in, and I'll send #bthem# a letter of recommendation. That will make it easier for you to advance! So, which job will you choose?\r\n#b#L1#I want to be a mighty Warrior!#l\r\n#L2#I want to be a mystical Magician!#l\r\n#L3#I want to be a sharp-shooting Bowman!#l\r\n#L4#I want to be a sneaky Thief!#l\r\n#L5#I want to be a swashbucking Pirate!#l");
	} else if (status == 2) {
	    sel = selection;
	    if (selection == 1) {
		qm.sendNext("A Warrior, huh? Boy, you're going to get really strong! They can take tons of damage, and dish plenty out, too. Okay, I'll send my recommendation to #bDance with Balrog#k, the Warrior Job Instructor.");
	    } else if (selection == 2) {
		qm.sendOk("Testting");
	    } else if (sel == 3) {
		qm.sendNext("You wanna be a Bowman? I hope you have really good aim! With their great dexterity, they have no problem avoiding attacks and firing off plenty of their own. Okay, I'll send my recommendation to #bAthena Pierce#k, the Bowman Job Instructor.");
	    } else if (selection == 4) {
		qm.sendOk("Thief");
	    } else if (selection == 5) {
		qm.sendOk("Testting");
	    }
	    qm.forceStartQuest(1406, sel);
	    qm.forceCompleteQuest();
	} else if (status == 3) {
	    if (sel == 1) {
		qm.sendNextPrev("He will contac when you reach Lv. 10. Become a great Warrior!");
	    } else if (sel == 2) {
	    } else if (sel == 3) {
		qm.sendNextPrev("She'll contact you once you reach #bLv. 10#k. I hope you become a magnificent Bowman!");
	    } else if (sel == 4) {
	    } else if (sel == 5) {
	} else if (status == 4) {
	    qm.dispose();
	}
    }
}

function end(mode, type, selection) {
}
/*
 * Copyright (C) 2002 by Michael Pitoniak (pitoniakm@msn.com)
 * All rights are reserved.
 * Reproduction and/or redistribution in whole or in part is expressly
 * prohibited without the written consent of the copyright owner.
 *
 * This Software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package utils.regex;


// TESTER:   https://regex101.com/r/mX51ru/1
	

	

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.apache.commons.io.ApacheCommonsIOServices;
import utils.array.ArrayServices;


public class RegExServices implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;
	
	
	
	public static RegExServices build() {
	    return new RegExServices();
	}
	
	private RegExServices() {}
	
	
	
	/*
	 * 	https://www.rexegg.com/regex-quantifiers.html
	 * 
	 * 
	 * there are 14 characters with special meanings: 
	 * backslash \ 
	 * caret ^
	 * dollar sign $
	 * period or dot .
	 * vertical bar or pipe symbol |
	 * question mark ?
	 * asterisk or star *
	 * plus sign +
	 * opening parenthesis (
	 * closing parenthesis )
	 * opening square bracket [
	 * closing square bracket ]
	 * opening curly brace {
	 * closing curly brace }
	 * These special characters are often called "metacharacters".
	 * 
	 * 
	 *  				\ ^ $ . | ? * + ( ) [ ] { }
	 *  
	 *  	.	Matches any single character.
	 * 		*	The preceding item will be matched zero or more times.
	 *		+	The preceding item will be matched one or more times.
	 *		? 	The question mark indicates there is zero or one of the preceding element. For example, colou?r matches both "color" and "colour".
	 * 
	 * 
	 */
	
	
	/*
	  	{N}	The preceding item is matched exactly N times.
		{N,}	The preceding item is matched N or more times.
		{N,M}	The preceding item is matched at least N times, but not more than M times.
		-	represents the range if it's not first or last in a list or the ending point of a range in a list.
		^	Matches the empty string at the beginning of a line; also represents the characters not in the range of a list.
		$	Matches the empty string at the end of a line.
		\b	Matches the empty string at the edge of a word.
		\B	Matches the empty string provided it's not at the edge of a word.
		\<	Match the empty string at the beginning of word.
		\>	Match the empty string at the end of word.

		
		
		NON GREEDY	
			If used immediately after any of the quantifiers *, +, ?, or {}, makes the quantifier 
			non-greedy (matching the fewest possible characters), 
			as opposed to the default, which is greedy (matching as many characters as possible). 
			For example, applying /\d+/ to "123abc" matches "123". 
			But applying /\d+?/ to that same string matches only the "1".
			
			+?  The ? makes the + "lazy" instead of "greedy". This means it tries to match as few times as possible, instead of trying to match as many times as possible.
			.*?  means "match zero or more characters but as little as possible". It is called a non-greedy match.
			
		    * - (0 or more) greedy matching
		    + - (1 or more) greedy matching
		    *? - (0 or more) non-greedy matching
		    +? - (1 or more) non-greedy matching
		    
		   	+-------------------+-----------------+------------------------------+
			| Greedy quantifier | Lazy quantifier |        Description           |
			+-------------------+-----------------+------------------------------+
			| *                 | *?              | Star Quantifier: 0 or more   |
			| +                 | +?              | Plus Quantifier: 1 or more   |
			| ?                 | ??              | Optional Quantifier: 0 or 1  |
			| {n}               | {n}?            | Quantifier: exactly n        |
			| {n,}              | {n,}?           | Quantifier: n or more        |
			| {n,m}             | {n,m}?          | Quantifier: between n and m  |
			+-------------------+-----------------+------------------------------+
			+	once or more
			A+	One or more As, as many as possible (greedy), giving up characters if the engine needs to backtrack (docile)
			A+?	One or more As, as few as needed to allow the overall pattern to match (lazy)
			A++	One or more As, as many as possible (greedy), not giving up characters if the engine tries to backtrack (possessive)
			*	zero times or more
			A*	Zero or more As, as many as possible (greedy), giving up characters if the engine needs to backtrack (docile)
			A*?	Zero or more As, as few as needed to allow the overall pattern to match (lazy)
			A*+	Zero or more As, as many as possible (greedy), not giving up characters if the engine tries to backtrack (possessive)
			?	zero times or once
			A?	Zero or one A, one if possible (greedy), giving up the character if the engine needs to backtrack (docile)
			A??	Zero or one A, zero if that still allows the overall pattern to match (lazy)
			A?+	Zero or one A, one if possible (greedy), not giving the character if the engine tries to backtrack (possessive)
			{x,y}	x times at least, y times at most
			A{2,9}	Two to nine As, as many as possible (greedy), giving up characters if the engine needs to backtrack (docile)
			A{2,9}?	Two to nine As, as few as needed to allow the overall pattern to match (lazy)
			A{2,9}+	Two to nine As, as many as possible (greedy), not giving up characters if the engine tries to backtrack (possessive)
			A{2,}
			A{2,}?
			A{2,}+	Two or more As, greedy and docile as above.
			Two or more As, lazy as above.
			Two or more As, possessive as above.
			A{5}	Exactly five As. Fixed repetition: neither greedy nor lazy.

	
		
		IMPORTANT:
		
		The dot matches a single character, without caring what that character is. The only exception are line break characters. 
		
		dotAll:
		By default, the dot '.' will not match line break characters.
	 	Specify this option to make the dot match all characters,
	 	including line breaks
		
		DOTALL=false
		
		.* does not collect \n
		
		.*\n does
		
		if you dont collect it some subsequent matches can get unexpected results
		

	
	*/
	
	/*
	 	USEFULL:
	 	
	 	
	 	"[.*\\n]" 	    strip first line
	 	
	 	.*$  catpures line but leave \n
	 	.*\n captures entire line with \n
	 	
	 	[A-Za-z0-9] 	Alphanumeric characters
		\\w 	[A-Za-z0-9_] 	Alphanumeric characters plus "_"
		\\W 	[^A-Za-z0-9_] 	Non-word characters
		\\a 	[A-Za-z] 	Alphabetic characters
		\\s 	[ \t] 	Space and tab
		\\b 	\< \> 	(?<=\W)(?=\w)|(?<=\w)(?=\W) 	Word boundaries
		[\x00-\x1F\x7F] 	Control characters
	 	\\d 	[0-9] 	Digits
		\\D 	[^0-9] 	Non-digits
		\\l 	[a-z] 	Lowercase letters
		\\h 	[ \t] Space and tab 	
		\\s 	 	[ \t\r\n\v\f] 	Whitespace characters. That is: \s matches a space, a tab, a line break, or a form feed.
		\\S 		[^ \t\r\n\v\f] 	Non-whitespace characters
		\\u 	[A-Z] 	Uppercase letters
		\\x 	[A-Fa-f0-9] 	Hexadecimal digits
	 */
	
	/*	
	  	dotAll:
		By default, the dot '.' will not match line break characters.
	 	Specify this option to make the dot match all characters,
	 	including line breaks
			
		multiLine:
	 	By default, the caret ^, dollar $ as well as \A, \z and \Z
	 	only match at the start and the end of the string.
	 	Specify this option to make ^ and \A also match after line breaks in the
	  	string, and make $, \z and \Z match before line breaks in the string
	
	*/
	
	/*
		\s                        # A space character.
		(\w{2})                   # Two alphabetic characters. Save as group 1.
		(?:\D*)                   # Any no-numeric characters. No save them.
		(\d+\S*)                  # From first digit found until a space. Save as group 2
	*/
	
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	public static final String TEMPLATE_VAR_REGEX = "\\{\\{\\s*(.*?)\\s*\\}\\}";
	
	
	public static final String  NEWLINE = "\r\n|\n|\r";
	
	
	/*8.8.8.8 via 10.3.8.1 dev eth0 src 10.3.8.24 
    cache*/
	public static final String DEFAULT_IPV4_REGEX =
			//".*?(\\d+\\.\\d+\\.\\d+\\.\\d+)";					//capture ip address in group 2, note: multiline=true to have ^ work after first line break
			".*?via\\s+(.*?)\\s+.*?src\\s+(.*?)\\s+";
	
	/*"([^\\s]+)\\s+" +  //one or more not white space followed by one or more white space
	"([^\\s]+)\\s+" + 
	"([^\\s]+)\\s+" + 
	"([^\\s]+)\\s+" + 
	"([^\\s]+)\\s+" + 
	"(.*)" //last field (command) can have spaces in it...*/
	/*Filesystem     1K-blocks    Used Available Use% Mounted on
	/dev/sda9        6275072 5359708    915364  86% /home*/
	public static final String DF_CWD_REGEX = 
			"[.*\\n]" +			//strip first line
			"([^\\s]+)\\s+" + 	//filesystem
			"[^\\s]+\\s+" +		//1k blocks
			"([^\\s]+)\\s+" +	//used
			"([^\\s]+)\\s+" +	//available
			"[^\\s]+\\s+" +		//percentage
			"([^\\s]+)$"; 		//mounted on
	/*
	 	enp0s25: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 10.0.0.4  netmask 255.255.255.0  broadcast 10.0.0.255
        inet6 2601:189:c600:2b0:1071:87bc:81e9:9dd  prefixlen 64  scopeid 0x0<global>
        inet6 fe80::e953:8b30:a3f1:3c25  prefixlen 64  scopeid 0x20<link>
        inet6 2601:189:c600:2b0:d2bf:9cff:fee0:623e  prefixlen 128  scopeid 0x0<global>
        ether d0:bf:9c:e0:62:3e  txqueuelen 1000  (Ethernet)
        RX packets 3910618  bytes 3576969084 (3.3 GiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 2547564  bytes 1592130375 (1.4 GiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
        device interrupt 20  memory 0xd0700000-d0720000  
 
	 */
	
	public static final String IFCONIG_REDHAT_REGEX = 	
			"(^[A-Za-z0-9]+):?" +  					//line starting with one or more chars captured in group 1 followed by zero or 1 colon (ubuntu has no colon)
			".*?inet\\s+" +							//non greed capture of any char (including newline with dotall=true) zero or more times followed by inet with a space
			"(?:addr:)?" +							//non capturing additional addr: prefix (ubuntu only) zero or 1 time
			"(\\d+\\.\\d+\\.\\d+\\.\\d+)" +			//capture ip address in group 2, note: multiline=true to have ^ work after first line break
			".*?netmask\\s+" +						//non greedy capture of netmask followed by space
			"(\\d+\\.\\d+\\.\\d+\\.\\d+)" +			//subnet mask	
			".*?ether\\s+" +						//non greedy capture up to ether follwerd by space
			"([0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+)";  //mac address
	
	
	/*
		 docker0   Link encap:Ethernet  HWaddr 02:42:2d:66:fc:f1  
	          inet addr:172.17.0.1  Bcast:0.0.0.0  Mask:255.255.0.0
	          inet6 addr: fe80::42:2dff:fe66:fcf1/64 Scope:Link
	          UP BROADCAST MULTICAST  MTU:1500  Metric:1
	          RX packets:2 errors:0 dropped:0 overruns:0 frame:0
	          TX packets:3 errors:0 dropped:0 overruns:0 carrier:0
	          collisions:0 txqueuelen:0 
	          RX bytes:152 (152.0 B)  TX bytes:258 (258.0 B)

		eth0      Link encap:Ethernet  HWaddr 08:00:27:31:65:b5  
		          inet addr:10.0.2.15  Bcast:10.0.2.255  Mask:255.255.255.0
		          inet6 addr: fe80::3db9:eaaa:e0ae:6e09/64 Scope:Link
		          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
		          RX packets:1089467 errors:0 dropped:0 overruns:0 frame:0
		          TX packets:508121 errors:0 dropped:0 overruns:0 carrier:0
		          collisions:0 txqueuelen:1000 
		          RX bytes:903808796 (903.8 MB)  TX bytes:31099448 (31.0 MB)
		
		lo        Link encap:Local Loopback  
		          inet addr:127.0.0.1  Mask:255.0.0.0
		          inet6 addr: ::1/128 Scope:Host
		          UP LOOPBACK RUNNING  MTU:65536  Metric:1
		          RX packets:9643 errors:0 dropped:0 overruns:0 frame:0
		          TX packets:9643 errors:0 dropped:0 overruns:0 carrier:0
		          collisions:0 txqueuelen:1 
		          RX bytes:719527 (719.5 KB)  TX bytes:719527 (719.5 KB)
	 */
	public static final String IFCONIG_UBUNTU_REGEX =
			"(^[A-Za-z0-9]+):?" +  			//line starting with one or more chars captured in group 1 followed by zero or 1 colon (ubuntu has no colon)
			"(?:.*?HWaddr\\s+)" + 
			"([0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+)" +
			".*?inet\\s+" +						//non capturing additional addr: prefix (ubuntu only)	
			"addr:?" +							//non greed capture of any char (including newline with dotall=true) zero or more times followed by inet with a space
			"(\\d+\\.\\d+\\.\\d+\\.\\d+)" +		//ip adder
			".*?Mask:?" +						//non greed capture of any char (including newline with dotall=true) zero or more times followed by Mask:
			"(\\d+\\.\\d+\\.\\d+\\.\\d+)" ;		//subnet mask
	
	
	/*
		Fedora release 27 (Twenty Seven)
		NAME=Fedora --OR-- "Red Hat Enterprise Linux Server"
		VERSION="27 (Workstation Edition)"
		ID=fedora
		VERSION_ID=27
		PRETTY_NAME="Fedora 27 (Workstation Edition)"
		ANSI_COLOR="0;34"
		CPE_NAME="cpe:/o:fedoraproject:fedora:27"
		HOME_URL="https://fedoraproject.org/"
		SUPPORT_URL="https://fedoraproject.org/wiki/Communicating_and_getting_help"
		BUG_REPORT_URL="https://bugzilla.redhat.com/"
		REDHAT_BUGZILLA_PRODUCT="Fedora"
		REDHAT_BUGZILLA_PRODUCT_VERSION=27
		REDHAT_SUPPORT_PRODUCT="Fedora"
		REDHAT_SUPPORT_PRODUCT_VERSION=27
		PRIVACY_POLICY_URL="https://fedoraproject.org/wiki/Legal:PrivacyPolicy"
		VARIANT="Workstation Edition"
		VARIANT_ID=workstation
		Fedora release 27 (Twenty Seven)
		Fedora release 27 (Twenty Seven)
	*/
	
	//STRIP QUOTES
	public static final String LINUX_DISTRO_REGEX = 
			"^NAME=" + 			//line that starts with NAME=
			"(?:[\"])?" + 		//non capturing group of zero or one quote char
			"([^\"|^\n]*)" + 	//capture group of all non quote or non \n
			"(?:[\"])?" + 		//non capturing group of zero or one quote char
			"(?:.*?)" +   		//non capturing group of non greedy any char
			"^VERSION_ID=" + 	//line that starts with VERSION_ID=
			"(?:[\"])?" +		//non capturing group of zero or one quote char
			"([^\"|^\n]*)" +	//capture group of all non quote or non \n	
			"(?:[\"])?";		//non capturing group of zero or one quote char
	
	/*lscpu
	Architecture:        x86_64
	CPU op-mode(s):      32-bit, 64-bit
	Byte Order:          Little Endian
	CPU(s):              4
	On-line CPU(s) list: 0-3
	Thread(s) per core:  2
	Core(s) per socket:  2
	Socket(s):           1
	NUMA node(s):        1
	Vendor ID:           GenuineIntel
	CPU family:          6
	Model:               60
	Model name:          Intel(R) Core(TM) i5-4200M CPU @ 2.50GHz
	Stepping:            3
	CPU MHz:             997.793
	CPU max MHz:         3100.0000
	CPU min MHz:         800.0000
	BogoMIPS:            4988.61
	Virtualization:      VT-x
	L1d cache:           32K
	L1i cache:           32K
	L2 cache:            256K
	L3 cache:            3072K
	NUMA node0 CPU(s):   0-3
	Flags:               fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx pdpe1gb rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 sdbg fma cx16 xtpr pdcm pcid sse4_1 sse4_2 movbe popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm abm cpuid_fault epb invpcid_single pti ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase tsc_adjust bmi1 avx2 smep bmi2 erms invpcid xsaveopt dtherm ida arat pln pts
	*/
	
	//set dotall=false
	public static final String CPU_INFO_REGEX = 
			"^Architecture:\\s+([^\\s]*)" +  	//starts with Archiecture followed by one or more white space follwed by any non white space 
			".*?" +								//non greedy capture of all chars (including newlines since dotall=true)
			"^CPU\\(s\\):\\s+([^\\s]*)" +		//starts with CPU (s) with '(' and ')' chars escaped, then white space, followed by capture group non space chars
			".*?" +								//non greedy capture of all chars (including newlines since dotall=true)
			"^CPU MHz:\\s+([^\\s]*)";			//stayrs with CPU MHz: follwed by whie space then capture group of no spece chars
	
	
	/*$ uname -a
	Linux inventor 2.4.20-gaming-r1 #1 Fri Apr 11 18:33:35 MDT 2003 i686 AMD Athlon(tm) XP 2100+ AuthenticAMD GNU/Linux

	More uname madness

	Now, let's look at the information that uname provides

	info. option                    arg     example
	kernel name                     -s      "Linux"
	hostname                        -n      "inventor"
	kernel release                  -r      "2.4.20-gaming-r1"
	kernel version                  -v      "#1 Fri Apr 11 18:33:35 MDT 2003"
	machine                         -m      "i686"
	processor                       -p      "AMD Athlon(tm) XP 2100+"
	hardware platform               -i      "AuthenticAMD"
	operating system                -o      "GNU/Linux"*/

	//Linux host-10-3-8-24 3.10.0-693.17.1.el7.x86_64 #1 SMP Sun Jan 14 10:36:03 EST 2018 x86_64 x86_64 x86_64 GNU/Linux
	public static final String UNAME_REGEX = 
			"([^\\s]*)" +  	//kernel name
			"\\s*" +       
			"([^\\s]*)" +  	//hostname
			"\\s*" +
			"([^\\s]*)" + 	//kernel release
			"\\s*" +
			"([^\\s]*\\s*[^\\s]*\\s*[^\\s]*\\s*[^\\s]*\\s*[^\\s]*\\s*[^\\s]*\\s*[^\\s]*\\s*[^\\s]*)" +  	//kernel version  #1 SMP Mon Jul 30 15:22:33 UTC 2018 
			"\\s*" +
			"([^\\s]*)" +	//machine
			"\\s*" +
			"([^\\s]*)" +	//processor
			"\\s*" +
			"([^\\s]*)" +	//hardware platform
			"\\s*" +
			"([^\\s]*)";	//operating system
	
	
	private static final String MAC_ADDRESS_REGEX = "([0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+)";
	
	private static final String SUBNET_MASK_REGEX = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
	
	//NNI-rhel7.5-kickstart-7.0.0-upgrade-1.sh
	public static final String NNOS_UPGRADE_VERSION_REGEX = ".*?kickstart-" +
			"(\\d+\\.\\d+\\.\\d+)" +
			".*";
	//NNI-rhel7.5-security-patch-set-2019-01-03.sh
	public static final String NNOS_SECURITY_PATCH_VERSION_REGEX = ".*?kickstart-" +
			"(\\d+\\.\\d+\\.\\d+)" +
			".*";
	private static final Pattern alphaNumeric = Pattern.compile("([^a-zA-z0-9])"); 
	private static final Pattern macPattern = Pattern.compile( "(([0-9a-fA-F]){1,2}[-:]){5}([0-9a-fA-F]){1,2}" );
	
	private static final Pattern subnetMaskPattern = Pattern.compile(SUBNET_MASK_REGEX);
	
	
	private static final Pattern ipPattern = Pattern.compile("\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");
	
	
	
	
	
	// The second dollar sign ensures matching the pattern only at the end of input. 
	//This one matches %, #, > and $.
	//[root@mpitoniak-rh7-01 ~]#   regex means ] char followed by any char %$#> at end of line
	public static final String PROMPT = ".*\\][%$#>] $";
	//[root@mpitoniak-rh7-01 ~]#   regex means any chars followed by any char %$#> at end of line
	public static final String PROMPT2 = ".*[%$#>] $";
	
	

	//newline in group1
	public static final String REGEX_FULL_LINE_INCLUDING_EOL = ".*(\n)?";
	
	

	/*
		.*? means "match zero or more characters but as little as possible". It is called a non-greedy match.
	
		.* means almost the same except that as much as possible is matched (greedy match).
	
		Examples:
	
		Using f.*?a to match foo bar baz results in foo ba (stops after first a)
		Using f.*a to match foo bar baz results in foo bar ba (stops after last a)
	
	  	By default, the dot will not match line break characters.
       	Specify this option to make the dot match all characters, including line breaks

	 	2017-03-02 14:32:38,182 ERROR utils.regex.RegExTest.exceptionRegExTest(RegExTest.java:300) - java.lang.ArithmeticException: / by zero
			at utils.regex.RegExTest.exceptionRegExTest(RegExTest.java:298)
			at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
			...
	 */
	public static final String REGEX_EXCEPTION = "^.+[Exception|Error][^\\n]++(\\s+at .++)+";

	public static final String REGEX_ANY_WHITE_SPACE = "\\s+";
	
	public static final String REGEX_ANY_WHITE_SPACE_NO_NEWLINE = "[ \\t\\r]*";
	
	//non capturing white space, this must use false for DOTALL
	public static final String SINGLE_LINE_NO_NEWLINE = ".*(?:\\s+)";
	
	public static final String REGEX_ANY_NEWLINE_AND_WHITE_SPACE = "[\\n\\r\\s]+";
	
	public static final String REGEX_NEWLINE_WINDOWS_UNIX = "(\r?\n)";
	
	//up to not including newline
	public static final String REGEX_UP_TO_NOT_INCLUDING_ZERO_OR_MORE_NEWLINE = ".*([\r\n])*";
	
	/*
    * By using a precompiled pattern we might speed things up, especially if there are multiple uses.
    */
	public static final Pattern  REGEX_RESERVED_CHARS = Pattern.compile("([\\\\*+\\[\\](){}\\$.?\\^|])");
	
	
	public static final String REGEX_FILE_EXTENSION = ".*(\\.txt|\\.csv)$";
	
	
	/*(?:              group, but do not capture (2 times):
	 \d{1,2}         digits (0-9) (between 1 and 2 times)
	  :              ':'
	  ){2}           end of grouping
	   \d{1,2}       digits (0-9) (between 1 and 2 times)
	   \.            '.'
	    \d{1,2}      digits (0-9) (between 1 and 2 times)*/
	
	public static final String REGEX_TIMESTAMP = "(?:\\d{1,2}:){2}\\d{1,2}\\.\\d{1,2}";


	
	/*
	 
         * '*' "zero or more"
        '+' "one or more"
        '?' "zero or one
        not greedy ? after any of these:  +  * } ?
    
        ex)
        
        .*?         any single char zero or more times not greedy
        .+?         any single char one or more times not greedy
        .??         any single char zero or one times not greedy
        .{2,6}?     The preceding item is matched at least N times, but not more than M times not greedy
        
	  	
	  	
	  	
	  	^((?:\\d+\\.?)+)(.*)

		break it down
		(?:\\d+\\.?)
		    one or more digits, optionally followed by a dot.  group the subpattern , but don't capture '?:'
		(above pattern)+
		    one or more of [digits followed by optional dot] capturured in $1
		(.*)
		   capture the rest in $2.
		
		Be aware that matching versions as above can cause issue with certain patterns due java being greedy or non-greedy in certain case.
		
		? 	The question mark indicates there is zero or one of the preceding element. For example, colou?r matches both "color" and "colour".
		
		?: tells the regexp expression to group the subpattern, but don't capture the match in $N (ie $1, $2, $3, etc).

		You have to group \\d+\\.? to be able to match 1 or more of the version sections.

		But you don't need to capture that group.  so it allows us to capture ALL of that group match as one group

	 */
	public static final String REGEX_VERSION_MATCH =  "^((?:\\d+\\.?)+)(.*)";
	
	public static final String REGEX_VERSION_MATCH2 = "^(?:(\\d+)\\.)?(?:(\\d+)\\.)?(\\d+)";
	
	public static final String REGEX_VERSION_MATCH3 = "(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)[^-]*(-SNAPSHOT)?.*";
	
	
	public static final String REGEX_VERSION_MATCH4 = "(\\d+)\\.(\\d+)\\.(\\d+)(\\.\\d+){0,}.*";
	
	public static final String REGEX_VERSION_MATCH5 = "(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:\\.(\\d+))?(?:\\.(\\d+))?[-]*(.*)";
	
	//captures 7, 7.1,  7.123, 77.88.99.6
	public static final String REGEX_VERSION_MATCH6 = "(\\d+)(\\.?\\d+)+";
	/*
	 *  
	 *  SET ptyType to dumb to get NO ansii escape chars
	 *  The right way to do this would be to make your shell not print these codes if there is no (or a dumb) terminal. 
	 *  But since they are often hard-coded in scripts (at least on my account here the prompt-colors are 
	 *  hard-coded in .bashrc), this might not be easy.
	 * 
	 */
	//private final String REGEX_ANSII_ESCAPE_SEQUENCE_CHARS = "\\[..;..[m]|\\[.{0,2}[m]|\u001B|\u000F";
	//private final String REGEX_ANSII_ESCAPE_SEQUENCE_CHARS = "\u001B\\[[;\\d]*m";
	public static final String REGEX_ANSII_ESCAPE_SEQUENCE_CHARS = "\u001B\\[[;\\d]*[ -/]*[@-~]";
	
	public static final String REGEX_NON_ASCII_CHARS = "\\P{InBasic_Latin}";
	
	
	public static final String DATA = 
			"aaaaaaaaaaaaaaa\n" +
			"bbbbbbbbbbbbbbb\n" +
			"ccccccccccccccc\n" +
			"2017/06/23 14:59:36,kernel,0,,NONDETERMINATE,\n" +
			"ddddddddddddddd\n" +
			"eeeeeeeeeeeeeee\n" +
			"2017/06/23 14:60:36,kernel,0,,INDETERMINATE,\n" +
			"ffffffffffffffff\n" +
			"gggggggggggggggg\n" +
			"iiiiiiiiiiiiiiii\n" +
			"hhhhhhhhhhhhhhhh\n" +
			"2017/06/23 14:61:36,kernel,0,,INDETERMINATE,\n" +
			"2017/06/23 14:62:36,kernel,0,,INDETERMINATE,\n" +
			"jjjjjjjjjjjjjjjjj\n" +
			"kkkkkkkkkkkkkkkkkk\n" +
			"llllllllllllllllll\n";
	
	
	//NEWLINE or no NEWLINE   .*(\\n)?
	//DOTALL=false .* does not match on \n
	public static final String LOGCAT_INDETERMINATE_ERRORS = "^[0-9]+/[0-9]+/[0-9]+.*?,kernel,0,.*?INDETERMINATE,.*(\\n)?";
	
	public static final String BEFORE_AFTER_LINES_LOGCAT_INDETERMINATE_ERRORS = "(.*[\\r\\n]+){0,4}^[0-9]+/[0-9]+/[0-9]+.*?,kernel,0,.*?INDETERMINATE,.*";
	
	
	public static final String NEGATIVE_LOOK_AHEAD = "^[0-9]+/[0-9]+/[0-9]+(?!.*?,kernel,0,.*?NONDETERMINATE,.*).+"; 
	
	//note if .*\n is placed in positive look ahead it is NOT stripped off...it looks like a positive lookahead needs something after it
	public static final String POSITIVE_LOOK_AHEAD = "^[0-9]+/[0-9]+/[0-9]+(?=.*?,kernel,0,.*?NONDETERMINATE,.*).+"; 
	
	
	//	'*' "zero or more"
	//	'+' "one or more"
	//	'?' "zero or one
    //  not greedy ? after any of these:  +  * } ?
	
	// [TEST] simple-test.sh over ridden default describe...
	// [SUBTEST] it_snapshot_Expected_Failure_subtest20_enable_snapshot_on_attached_disk_C74454: [FAIL]
	public static final String ROUNDUP_PARSER_REGEX = 
			 "^\\s*" + 														// starts with zero or more white space
	    	"\\[(TEST|TEST_DESCR|SUBTEST|SUBTEST_DESCR|ERROR|DESCR)\\]" + 	// [ followed by TEST, SUBTEST, or ERROR followed by ] (captured in group 1)
	    	"\\s*" + 														// zero or more white spaces
	    	"([^:]+?)(?:_C(\\d+))?" + 										// any non ':" char one or more times not greedy in group2 followed by zero or more testcasiId's captured in group3
	    	"(?::|$)" + 													// non capturing group  of a ':" or end of line
	    	"\\s*" + 														// zero or more white spaces
	    	"(?:\\[(PASS|FAIL|SKIP)\\])?"; 									// ?: creates a non capturing parentheses group (with part of the expression captured in group 4) ? -> zero or one times
				

	
	
	
	
    //	'*' "zero or more"
	//	'+' "one or more"
	//	'?' "zero or one
    //  not greedy ? after any of these:  +  * } ?
	// "[TEST] simple2"
    // "[SUBTEST] it io subtest1 C12345:    [PASS]\n"  WHERE C12345 may or may not exist
	 public static final String ROUNDUP_PARSER_REGEX_OLD = 
			"^\\s*" + 														//starts with zero or more white space
	    	"\\[(TEST|TEST_DESCR|SUBTEST|SUBTEST_DESCR|ERROR|DESCR)\\]" + 	// [ followed by TEST, SUBTEST, or ERROR followed by ] (captured in group 1)
	    	"\\s*" + 														//zero or more white spaces
			"([^:]+?)(?:\\s+C(\\d+))?\\s*(?::|$)" +
	    	"\\s*" + 														//zero or more white spaces
	    	"(?:\\[(PASS|FAIL|SKIP)\\])?"; 									// ?: creates a non capturing parentheses group (with part of the expression captured in group 4) ? -> zero or one times

	 
	 public static final String UB_REQUEST = 
				"GET http://10.7.3.130:9443/ HTTP/1.1\r\n" +
				"User-Agent: GBA-service; 0.1; 3gpp-gba-uicc\r\n" +
				"Connection: Keep-Alive\r\n" +
				"Accept-Encoding: gzip, deflate\r\n" +
				"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
				"Accept-Language: en-US,en;q=0.5\r\n" +
				"Authorization: Digest username=\"001019990000000@ims.mnc001.mcc001.3gppnetwork.org\", realm=\"ims.mnc001.mcc001.pub.3gppnetwork.org\", nonce=\"\", opaque=\"dynamic_opaque\", uri=\"http://10.7.3.130:9443/\", response=\"\"\r\n" +
				"Host: bsf.ims.mnc001.mcc001.pub.3gppnetwork.org\r\n" +
				"Proxy-Connection: Keep-Alive\r\n" +
				"Content-Length: 0\r\n\r\n";
	 
	 public static final String IOPS_REGEX = "iops=(\\d+)\\s";
	 
	 /*public final String ROUNDUP_PARSER_REGEX_NEW = 
    		"^\\s*" + //starts with zero or more white space
    		"\\[(TEST|TEST_DESCR|SUBTEST|SUBTEST_DESCR|ERROR|DESCR)\\]" + // [ followed by TEST, SUBTEST, or ERROR followed by ] (captured in group 1)
    		"\\s*" + 							//zero or more white spaces
			"([^:]+)+?" +						// match one or more characters that are not a colon, but do lazy (not greedy) matching, put in group 2
    		"(?:\\s+C(\\d+))?\\s*:*" +  		// group the subpattern but don't capture in a group
    		"\\s*" + 							//one or more white spaces
    		"(?:\\[(PASS|FAIL|SKIP)\\])?"; 		// ?: creates a non capturing parentheses group (with part of the expression captured in group 4) ? -> zero or one times
*/

	//"Tests:   54 | Passed:   21 | Failed:   33";  
	public static final String ROUNDUP_END_MARKER_REGEX = 
		"^Tests:\\s*" +
		"(\\d*)" + 		//zero or more decimal chars
		"\\s*" + 		//zero or more white spaces
		"\\|" + 		// | char
		"\\s*" +		//zero or more white spaces
		"Passed:" +		//Passed:
		"\\s*" +		//zero or more white spaces
		"(\\d+)" +		//zero or more decimal chars
		"\\s*" +		//zero or more white spaces
		"\\|" +			// | char
		"\\s*" +		//zero or more white spaces
		"Failed:" +		//Failed:
		"\\s*" +
		"(\\d+)";		//zero or more decimal chars
	
	//matches all Exception: followed by 2013-01-16 18:31:14
	public String parseLogForExceptions(String logFilePath) throws Exception {
		return parseLogForRegionMatches(logFilePath, "Exception:", "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}");	
	}
	
	/*
	 * 
	 * 		***********************   VERY POWERFUL PATTERN   ***********************
	 * 
	 */
	public String parseLogForRegionMatches(String logFilePath, String startRegEx, String endRegEx) throws Exception {
		String data = ApacheCommonsIOServices.build().readFileToString(logFilePath);
		return parseStringForRegionMatches(data, startRegEx, endRegEx);
	}
	
	public String parseStringForExceptions(String data) throws Exception {
		return parseStringForRegionMatches(data, "Exception:", "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}");
	}
	
	/*
	 * 
	 * 		***********************   VERY POWERFUL PATTERN   ***********************
	 * 		returns all lines that start with startRegEx, and end with endRegEx
	 * 
	 */
	public String parseStringForRegionMatches(String data, String startRegEx, String endRegEx) {
		StringBuffer responseBuffer = null;
		StringBuffer tempBuffer = null;
		Pattern beginPattern = null;
		Pattern endPattern = null;
		String[] lines = null;
		boolean collecting = false;
		String strLine = null;

		responseBuffer = new StringBuffer();
		tempBuffer = new StringBuffer();
		beginPattern = Pattern.compile(startRegEx);
		endPattern = Pattern.compile(endRegEx);
		lines = data.split(NEWLINE);
		
		for(int i=0; i<lines.length; i++) {
			strLine = lines[i];
			Matcher beginMatcher = beginPattern.matcher(strLine);
			Matcher endMatcher = endPattern.matcher(strLine);

			boolean beginMatch = beginMatcher.find();
			boolean endMatch = endMatcher.find();
			
			if(!collecting && beginMatch){
				tempBuffer = new StringBuffer();
				collecting = true;
			}
			if(collecting && endMatch){
				responseBuffer.append(tempBuffer.toString() + strLine + "\n\n");
				collecting = false;
			}if(collecting && !endMatch){
				tempBuffer.append(strLine + "\n");
			}else if(collecting && !endMatch){
				tempBuffer.append(strLine + "\n");
			}
		}
		
		return responseBuffer.toString();
	}
	
	public String replaceAllNonAsciiChars(String str){
		return str.replaceAll(REGEX_NON_ASCII_CHARS, "");
	}
	
	public String replaceAllANSIIEscapeSequenceChars(String inStr){
		return inStr.replaceAll(REGEX_ANSII_ESCAPE_SEQUENCE_CHARS, "");
	}

	public String normalizePath(String path){
        String normalizedPath = path;
        
        if(path.startsWith("~")){
            normalizedPath = path.replaceFirst("^~", System.getProperty("user.home"));
        }else if(!path.equals(".")){
            normalizedPath = FilenameUtils.normalizeNoEndSeparator(path);
        }
        
        return normalizedPath;
    }

	
	/*
	 * This function will escape special characters within a string to ensure
	 * that the string will not
	 * 
	 * be parsed as a regular expression. This is helpful with accepting using
	 * input that needs to be used
	 * 
	 * in functions that take a regular expression as an argument (such as
	 * String.replaceAll(), or String.split()).
	 * 
	 * @param inStr - argument which we wish to escape.
	 * 
	 * @return - Resulting string with the following characters escaped:
	 * [](){}+^?$.\
	 */
	public String escapeRegEx(String inStr) {
		Matcher match = REGEX_RESERVED_CHARS.matcher(inStr);
		
		return match.replaceAll("\\\\$1");
	}
	
	public boolean test(String matcStr, String regEx){
		return matcStr.matches(regEx);
	}
	
	
	//	ex:	0.1.3.4dhskfjhasjfhsjfhj     hyper_test_0.0.4
	public String getVersion(String matcStr){
		return find(matcStr, REGEX_VERSION_MATCH, true, false, false, 1);
	}
	
	
	
	
	/**
	 * 
	 * This procedure is used to ensure that a string is not interpreted as a
	 * regular expression.
	 * 
	 * This is useful when using the String.replaceAll() or String.split()
	 * functions with user input. This will escape
	 * 
	 * special characters from the input string so that it will not be
	 * interpreted as a regular expression.
	 * 
	 * This alternate version does not use a precompiled pattern so it may be
	 * copied and pasted into your code
	 * 
	 * as is.
	 * 
	 * @param strIn
	 *            - a string that we want to ensure does not interfere with a
	 *            regular expression.
	 * 
	 * @return - strIn with the following chars escaped: [](){}+*^?$.\
	 */
	public String escRegEx(String inStr) {
		// will replace the char with \char
		return inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1");
	}
	
	
	//Recursive Grep
	//returns an ArrayList of hits in the following format:
	//path:complete line where hit occurred
	public List<String> grepDir(String folder, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) throws Exception{
		Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
		return grepDir(folder, pattern, new ArrayList<>());
	}

	//returns an ArrayList of hits in the following format:
	//filepath:complete line where hit occurred
	private List<String> grepDir(String folder, Pattern pattern, ArrayList<String> arrayList) throws Exception{
		File file = new File(folder);
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			if(files != null){
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						grepFile(files[i], pattern, arrayList);
					} else if (file.isDirectory()) {
						grepDir(files[i].getPath(), pattern, arrayList);
					}
				}
			}
		}
		
		return arrayList;
	}
	
	public int grepFile(File f, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, ArrayList<String> arrayList) throws Exception{
		return grepFile(f, getPattern(regEx, caseSensitve, dotAll, multiLine), arrayList);
	}
	
	// TODO replace this with next method
	// this method deals with lines, while the next deals with hit locations
	// Converts the contents of a file into a CharSequence
    // suitable for use by the regex package.
	public int grepFile(File f, Pattern pattern, List<String> arrayList) throws IOException{
        FileInputStream fis = null;
        FileChannel fc = null;
        Matcher matcher = null; 
		LineNumberReader lineReader = null;
		String line = null;
    
		try{
			fis = new FileInputStream(f.getPath());
	        fc = fis.getChannel();
	        // Create a read-only CharBuffer on the file
	        ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
	        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
	        //Create matcher on file
	        matcher = pattern.matcher(cbuf);
	        //we reopen the file and read it line by line to 
			//get the line number for each match
	        lineReader = new LineNumberReader(new FileReader(f));
	    
	        while ((line = lineReader.readLine()) != null) {
				matcher.reset(line); // reset the input
				if (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					arrayList.add(convertToForwardSlash(f.getPath()) + "<END_PATH>" + lineReader.getLineNumber()
							+ ":" + line);
				}
			}
		} finally {
			ApacheCommonsIOServices.closeQuietly(fis);
			ApacheCommonsIOServices.closeQuietly(lineReader);
		}
		
		return arrayList.size();
    }
	
	public String convertToForwardSlash(String backSlasStr) {
		// Strings with backslashes come in like this:
		// "folder\\subfolder"
		// In order to replace "\\" we must use "\\\\"
		return backSlasStr.replaceAll("\\\\", "/");
	}

    /**
     * Substitute prefix, e.g. ".*world.*" by "universe"
     *
     * @param filePath file path which sed shall be applied
     * @param prefixSubstitute Prefix which shall be replaced
     * @param substituteReplacement Prefix which is going to replace the original
     *
     * @throws IOException DOCUMENT ME!
     */
	/*
		 Memory-mapped files allow you to create and modify files that are too big to bring into memory. 
		 With a memory-mapped file, you can pretend that the entire file is in memory and that you can access 
		 it by simply treating it as a very large array. This approach greatly simplifies the code you write 
		 in order to modify the file. The file appears to be accessible all at once because only portions of 
		 it are brought into memory, and other parts are swapped out. This way a very large file (up to 2 GB) 
		 can easily be modified. Note that the file-mapping facilities of the underlying operating system are 
		 used to maximize performance. 
	 */
	
	//note: never got this to work...memory mapped errors
    public void replaceAllMemoryMap(String filePath, String replaceStr, String regEx, String replacement, boolean caseSensitve, boolean dotAll, boolean multiLine)
		throws IOException {
    	File file = null;
    	FileInputStream fis = null;
    	FileChannel fc = null;
    	FileOutputStream fos = null;
    	PrintStream ps = null;
    	Pattern pattern = null;
    	
    	try{
	    	file = new File(filePath);
	    	
	    	pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
	
	        // Open the file and then get a channel from the stream
	        fis = new FileInputStream(file);
	        fc = fis.getChannel();
	
	        // Get the file's size and then map it into memory
	        int sz = (int)fc.size();
	        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, 0, sz);
	        // Decode the file into a char buffer
	        // Charset and decoder for ISO-8859-15
	        Charset charset = Charset.forName("ISO-8859-15");
	        CharsetDecoder decoder = charset.newDecoder();
	        CharBuffer cb = decoder.decode(bb);
	
	        Matcher matcher = pattern.matcher(cb);
	        String outString = matcher.replaceAll(replaceStr);
	        fos = new FileOutputStream(file.getAbsolutePath());
	        ps = new PrintStream(fos);
	        ps.print(outString);
    	}finally{
    		ApacheCommonsIOServices.closeQuietly(ps);
    		ApacheCommonsIOServices.closeQuietly(fos);
    		ApacheCommonsIOServices.closeQuietly(fc);
    		ApacheCommonsIOServices.closeQuietly(fis);
    	}
    }
 
    public List<File> listFiles(String directory, boolean recursive, String... extensions){
		return (List<File>)FileUtils.listFiles(new File(normalizePath(directory)), extensions, recursive);
	}

    /*
 	Reads the contents of a file into a String using the default encoding for the VM. The file is always closed.
	
		Parameters:
			file - the file to read, must not be null 
		Returns:
			the file contents, never null 
		Throws:
			IOException - in case of an I/O error
	 */
	public String readFileToString(File file) throws IOException{
		return FileUtils.readFileToString(file, Charset.defaultCharset());
	}

    public String replaceAllNewLinesAndCommas(String str){
    	return str.replaceAll("(\n|,)" , "");
    }
	
	/*
	 * Uses split to break up a string of input separated by
	 * commas and/or whitespace.
	 * "\\s+" splist on white space(s)
	 */
	public String[] split(String str) throws IOException {
        // Create a pattern to match breaks
        Pattern p = Pattern.compile("[,\\s]+");
        // Split input with the pattern
        return p.split(str);
	}

	public String regExTester(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) throws IOException {
    	return findAllMatches(input, regEx, caseSensitve, dotAll, multiLine);
    }
	

	/* The matching routines in java.util.regex require that the input be a CharSequence 
	 * object. This implements a method that efficiently returns the contents of a file 
	 * in a CharSequence object. 
	 * 
	 * Converts the contents of a file into a CharSequence 
	 * suitable for use by the regex package. 
	 */
	public List<String> findInFile(String filePath, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) throws Exception{
		List<String> arrayList = new ArrayList<>();
		Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
		FileInputStream fis = null;
        FileChannel fc = null;
        
        try {
			fis = new FileInputStream(filePath);
	        fc = fis.getChannel();
	        // Create a read-only CharBuffer on the file
	        ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
	        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
	        grepCharBuffer(cbuf, pattern, arrayList);
        }finally {
        	ApacheCommonsIOServices.closeQuietly(fis);
        }
	        
		return arrayList;
	}
	
	//populates an ArrayList of entries start:end match locations
	private void grepCharBuffer(CharBuffer charBuffer, Pattern pattern, List<String> arrayList) throws Exception{
        Matcher matcher = null; 
    
        matcher = pattern.matcher(charBuffer);
        
        while (matcher.find()) {
        	//Get the matching string
        	
        	/*Returns the input subsequence matched by the previous match. 
        	 For a matcher m with input sequence s, the expressions m.group() 
        	 and s.substring(m.start(), m.end()) are equivalent. 
        	 Note that some patterns, for example a*, match the empty string. 
        	 This method will return the empty string when the pattern successfully 
        	 matches the empty string in the input.*/
            String match = matcher.group();
			int start = matcher.start();
			int end = matcher.end();
			arrayList.add(start + ":" + end);
		}
    }

	public String findAllInFile(String filePath, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) throws IOException {
		return findAllMatches(ApacheCommonsIOServices.build().readFileToString(filePath), regEx, caseSensitve, dotAll, multiLine);
	}
	/*	
	  	dotAll:
		By default, the dot '.' will not match line break characters.
	 	Specify this option to make the dot match all characters,
	 	including line breaks
			
		multiLine:
	 	By default, the caret ^, dollar $ as well as \A, \z and \Z
	 	only match at the start and the end of the string.
	 	Specify this option to make ^ and \A also match after line breaks in the
	  	string, and make $, \z and \Z match before line breaks in the string
	*/
	public String find(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) {
		return find(input, regEx, caseSensitve, dotAll, multiLine, null);
	}
	
	/*
	 * Returns the FIRST match or regEx or a specific group
	 * 
	 * Capturing groups are indexed from left to right, starting at one. Group zero denotes the entire pattern, 
	 * so the expression m.group(0) is equivalent to m.group().
	 * If the match was successful but the group specified failed to match any part of the input sequence, 
	 * then null is returned. Note that some groups, for example (a*), match the empty string. This method 
	 * will return the empty string when such a group successfully matches the empty string in the input.
	 */
	public String find(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, Integer group) {
		String response = null;
		Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
		Matcher matcher = pattern.matcher(input);
		
		/*
			Attempts to find the next subsequence of the input sequence that matches the pattern.
			This method starts at the beginning of this matcher's region, or, if a previous invocation of the 
			method was successful and the matcher has not since been reset, at the first character not matched 
			by the previous match.
			If the match succeeds then more information can be obtained via the start, end, and group methods.
			Returns:
			true if, and only if, a subsequence of the input sequence matches this matcher's pattern
		*/
		if (matcher.find()) {
			if(group!=null){
				response = matcher.group(group);
			}else{
				response = matcher.group();
			}
		}

		return response;
	}
	
	public List<RegExMatch> findAllMatchers(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) {
		List<RegExMatch> matchList = new ArrayList<RegExMatch>();
		Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()) {
        	matchList.add(new RegExMatch(matcher));
        }
        
        return matchList;
    }
	
	public List<RegExMatch> findAllRegExMatch(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) {
		List<RegExMatch> matchList = new ArrayList<RegExMatch>();
		Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()) {
        	matchList.add(new RegExMatch(matcher));
        }
        
        return matchList;
    }
	
	public List<String> findAllTemplateTags(String templateFilePath) throws IOException, RegExException{
		List<String> tagList = new ArrayList<>();
		ApacheCommonsIOServices apacheCommonsIOServices = ApacheCommonsIOServices.build();
		Matcher matcher = RegExServices.build().findAll(apacheCommonsIOServices.readFileToString(templateFilePath), TEMPLATE_VAR_REGEX, true, false, false);//non greed capture of any char (including newline with dotall=true) zero or more times 
		while(matcher.find()) {
			if(!tagList.contains(matcher.group(1))){
				tagList.add(matcher.group(1));
			}
		}
		return tagList;
	}
	
	public List<RegExMatch> findAllRegExMatchInFile(String filePath, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) throws IOException {
		return findAllRegExMatch(ApacheCommonsIOServices.build().readFileToString(filePath), regEx, caseSensitve, dotAll, multiLine);
	}
	
	public String insertStringInFile(String filePath, String insertStr, String regEx, int group, boolean before, boolean after, boolean caseSensitve, boolean dotAll, boolean multiLine) throws IOException {
		ApacheCommonsIOServices apacheCommonsIOServices = ApacheCommonsIOServices.build();
		String fileContents = apacheCommonsIOServices.readFileToString(filePath);
		String newStr = modifyString(fileContents, insertStr, regEx, group, before, after, caseSensitve, dotAll, multiLine);
		apacheCommonsIOServices.write(filePath, newStr, false);
		
		return newStr;
	}

	public String modifyString(String originalString, String insertStr, String regEx, Integer group, Boolean before, Boolean after, Boolean caseSensitve, Boolean dotAll, Boolean multiLine) throws IOException {
    	String newStr = null;

		if(before!=null && before) {
			if(group!=null) {
				newStr = insertBeforeGroup(originalString, regEx, caseSensitve, dotAll, multiLine, insertStr, group);
			}else {
		        newStr = insertBeforeGroup(originalString, regEx, caseSensitve, dotAll, multiLine, insertStr, 0);
			}
		}else if(after!=null && after) {
			if(group!=null) {
				newStr = insertAfterGroup(originalString, regEx, caseSensitve, dotAll, multiLine, insertStr, group);
			}else {
		        newStr = insertAfterGroup(originalString, regEx, caseSensitve, dotAll, multiLine, insertStr, 0);
			}
		}else {
			newStr = replaceGroup(originalString, regEx, caseSensitve, dotAll, multiLine, insertStr, 0);
		}
		return newStr;
	}

	private void displayMatcher(String regEx, Matcher matcher) {
		LOGGER.debug("groupCnt: {} {}", regEx, matcher.groupCount());
		LOGGER.info("MATCHER {}: {}",0,  matcher.group(0));
		for(int i=1; i<=matcher.groupCount(); i++) {
			LOGGER.info("MATCHER {}: {}",i,  matcher.group(i));
		}
	}
	
	
	/*
	 * Capturing groups are indexed from left to right, starting at one. Group zero denotes the entire pattern, 
	 * so the expression m.group(0) is equivalent to m.group().
	 * If the match was successful but the group specified failed to match any part of the input sequence, 
	 * then null is returned. Note that some groups, for example (a*), match the empty string. This method 
	 * will return the empty string when such a group successfully matches the empty string in the input.
	 */
    public String findAllMatches(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) throws IOException {
    	StringBuffer sb = new StringBuffer();
    	
    	if(input==null) {
    		throw new IOException("input data is null");
    	}
    	Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
        Matcher matcher = pattern.matcher(input);
        int matchCnt = 0;
        while(matcher!=null && matcher.find()){
        	if(matchCnt==0) {
        		sb.append("\n");
        	}
    		sb.append("------------------" + "match: " +  ++matchCnt + "------------------\n");
    		for(int i=1; i<=matcher.groupCount(); i++) {
    			sb.append("group: " + i + ": " + matcher.group(i) + "\n");
    		}
        }
        if(sb.toString().trim().length()>0) {
        	sb.append("--------------------------------------------");
        }
        return sb.toString();
    }
    
    public Matcher findAll(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) throws RegExException {
    	return findAll(input, regEx, caseSensitve, dotAll, multiLine, false);
    }

	public Matcher findAll(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, boolean failOnNoMatch) throws RegExException {
        Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
        Matcher matcher = pattern.matcher(input);
        if(matcher.find()) {
        	return matcher;
        }else {
        	if(failOnNoMatch) {
        		throw new RegExException("match not found");
        	}
        }
        return matcher;
    }
	
	
	/*
	 * Returns a hashmap of all matches and their respective start indexes
	 * 
	 * Capturing groups are indexed from left to right, starting at one. Group zero denotes the entire pattern, 
	 * so the expression m.group(0) is equivalent to m.group().
	 * If the match was successful but the group specified failed to match any part of the input sequence, 
	 * then null is returned. Note that some groups, for example (a*), match the empty string. This method 
	 * will return the empty string when such a group successfully matches the empty string in the input.
	 */
    public LinkedHashMap<Integer, String> findAllMap(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) {
    	LinkedHashMap<Integer, String> hashMap = new LinkedHashMap<Integer, String>();
    	Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()){
        	hashMap.put(matcher.start(), matcher.group());
        }
        
        return hashMap;
    }

    public String insertBeforeGroup(String inputStr, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, String insertStr, int group) {
	   	 return modifyGroup(inputStr, regEx, caseSensitve, dotAll, multiLine, insertStr, group, REPLACE_TYPE.BEFORE);
	}
    
    public String insertAfterGroup(String inputStr, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, String insertStr, int group) {
	    return modifyGroup(inputStr, regEx, caseSensitve, dotAll, multiLine, insertStr, group, REPLACE_TYPE.AFTER);
	}
	
	public String replaceGroup(String inputStr, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, String insertStr, int group) {
	    return modifyGroup(inputStr, regEx, caseSensitve, dotAll, multiLine, insertStr, group, REPLACE_TYPE.REPLACE);
	}
	
	public String deleteGroup(String inputStr, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, int group) {
	    return modifyGroup(inputStr, regEx, caseSensitve, dotAll, multiLine, null, group, REPLACE_TYPE.DELETE);
	}
	
	public String modifyGroup(String inputStr, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, String insertStr, int group, REPLACE_TYPE replaceType) {
		Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
	    Matcher matcher = pattern.matcher(inputStr);
	    String preMatch = null;
	    String groupMatch = null;
	    String postMatch = null;
	    StringBuffer sb = new StringBuffer();
	    String line = null;
	    boolean match = false;
	    
	    while (matcher.find()) {
	    	match=true;
	    	preMatch = inputStr.substring(matcher.start(), matcher.start(group));
	    	groupMatch = inputStr.substring(matcher.start(group), matcher.end(group));
	    	postMatch = inputStr.substring(matcher.end(group), matcher.end());
	    	line = preMatch;
	    	if(replaceType.equals(REPLACE_TYPE.BEFORE)) {
	    		line+= insertStr;
	    	}
	    	if(replaceType.equals(REPLACE_TYPE.REPLACE)){
	    		line+= insertStr;
	    	}else{
		    	if(!replaceType.equals(REPLACE_TYPE.DELETE)){
		    		line+= groupMatch;
		    	}
	    	}
	    	if(replaceType.equals(REPLACE_TYPE.AFTER)) {
	    		line+= insertStr;
	    	}
	    	line+= postMatch;
	    	matcher.appendReplacement(sb, line);
	    }
	    matcher.appendTail(sb); 
	    return sb.toString();
	}
    
    public String replaceAllGroups(String inputStr, String regEx, String replacement, int group) {
	    Pattern pattern = Pattern.compile(regEx);
	    Matcher matcher = pattern.matcher(inputStr);
	    StringBuffer sb = new StringBuffer();
	    while (matcher.find()) {
	        StringBuffer buf = new StringBuffer(matcher.group());
	        buf.replace(matcher.start(group)-matcher.start(), matcher.end(group)-matcher.start(), replacement);
	        matcher.appendReplacement(sb, buf.toString());
	    }
	    matcher.appendTail(sb); 
	    return sb.toString();
	}
    
    public String replaceGroup(String source, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, int groupToReplace, String replacement) {
        return replaceGroup(source, regEx, caseSensitve, dotAll, multiLine, groupToReplace, 1, replacement);
    }

    public String replaceGroup(String source, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine, int groupToReplace, int groupOccurrence, String replacement) {
    	Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
    	Matcher m = pattern.matcher(source);
        for(int i = 0; i < groupOccurrence; i++){
            if (!m.find()) {
            	return source; // pattern not met, may also throw an exception here
            }
        }
        return new StringBuilder(source).replace(m.start(groupToReplace), m.end(groupToReplace), replacement).toString();
    }
	
	//Returns a String with regex replacement...
    public String replaceAll(String input, String regEx, String replacement, boolean caseSensitve, boolean dotAll, boolean multiLine) {
    	return getPattern(regEx, caseSensitve, dotAll, multiLine).matcher(input).replaceAll(replacement);
    }
    
    public String replaceAll2(String input, String regEx, String replacement, boolean caseSensitve, boolean dotAll, boolean multiLine) {
    	Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        int currentStart = 0;
        int currentEnd = 0;
        while(matcher.find()) {
        	sb.append(input.substring(currentStart, matcher.start()));
        	sb.append(replacement);
        	currentStart = currentEnd = matcher.end();
        }
        sb.append(input.substring(currentEnd, input.length()));
        
        return sb.toString();
    }
    
	public String replaceAllInFile(String filePath, String regEx, String replaceStr) throws IOException {
		return replaceAllInFile(filePath,  regEx, replaceStr, true, true, true);
	}
	
	public String replaceAllInFile(String filePath, String matchRegEx, String replacement, boolean caseSensitve, boolean dotAll, boolean multiLine) throws IOException {
		ApacheCommonsIOServices apacheCommonsIOServices = ApacheCommonsIOServices.build();
		File tempFile = new File(filePath);
		if(!tempFile.exists()) {
			throw new FileNotFoundException(filePath);
		}
	    String contents = apacheCommonsIOServices.readFileToString(filePath);
	    contents = replaceAll(contents, matchRegEx, replacement, caseSensitve, dotAll, multiLine);
	    apacheCommonsIOServices.write(contents, filePath, false);
	    
	    return contents;
	}
	
	public String replaceAllInFile(String filePath, Map<String, String> map, boolean caseSensitve, boolean dotAll, boolean multiLine) throws IOException {
		ApacheCommonsIOServices apacheCommonsIOServices = ApacheCommonsIOServices.build();
		File tempFile = new File(filePath);
		if(!tempFile.exists()) {
			throw new FileNotFoundException(filePath);
		}
	    String contents = apacheCommonsIOServices.readFileToString(filePath);
	    for(String key : map.keySet()) {
	    	contents = replaceAll(contents, key, map.get(key), caseSensitve, dotAll, multiLine);
	    }
	    apacheCommonsIOServices.write(filePath, contents, false);
	    
	    return contents;
	}

    //Returns boolean indicating if regex pattern match exists in input String
    //default regex behaviour is case sensitive
    public boolean matches(String input, String regEx) {
    	return matches(input, regEx, false);
    }
    
    //Returns boolean indicating if regex pattern match exists in input String
    public boolean matches(String input, String regEx, boolean caseSensitve) {
    	return matches(input, regEx, caseSensitve, true, true);
    }
    
    //Returns boolean indicating if regex pattern match exists in input String
    public boolean matches(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) {
    	Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
    	Matcher matcher = pattern.matcher(input);
    	
    	return matcher.find();
    }
     
    private String escapeNonAlphaNumeric(String subjectString){  
    	 return alphaNumeric.matcher(subjectString).replaceAll("\\\\$1");  
    }
    
    public boolean isMacAddress(String macAddr){
    	Matcher m = macPattern.matcher(macAddr);
		return m.matches();
    }
    
    public boolean isIpAddress(String macAddr){
    	Matcher m = ipPattern.matcher(macAddr);
		return m.matches();
    }  
    
    public boolean isSubnetMask(String subnetMask){
    	Matcher m = subnetMaskPattern.matcher(subnetMask);
		return m.matches();
    }  
    
    /*
     	It's much easier to use a java.util.regex.Matcher and do a find() rather than any kind of split 
     	in these kinds of scenario.

		That is, instead of defining the pattern for the delimiter between the tokens, you define the pattern 
		for the tokens themselves.
		
		The pattern is essentially:

		"([^"]*)"|(\S+)
		 \_____/  \___/
		    1       2
		
		There are 2 alternates:

    	The first alternate matches the opening double quote, a sequence of anything but double quote (captured in group 1), 
    	then the closing double quote
    	
    	The second alternate matches any sequence of non-whitespace characters, captured in group 2
    	The order of the alternates matter in this pattern

		Note that this does not handle escaped double quotes within quoted segments. If you need to do this, then the pattern 
		becomes more complicated, but the Matcher solution still works.
    
     */
    public String[] splitQuotedArgs(String str){
    	List<String> list = new ArrayList<>();
    	
    	String regex = "\"([^\"]*)\"|(\\S+)";

        Matcher m = Pattern.compile(regex).matcher(str);
        while (m.find()) {
            if (m.group(1) != null) {
            	list.add( m.group(1) );
            } else {
            	list.add( m.group(2));
            }
        }

        return ArrayServices.build().listToStringArray(list);
    }
    
	private Pattern getPattern(String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine){
		int options = 0;
        
        /*Enables case-insensitive matching. 
         By default, a pattern is case-sensitive. By adding a flag, a pattern can be made case-insensitive.
		 It is also possible to control case sensitivity within a pattern using the inline modifier (?i). 
		 The inline modifier affects all characters to the right and in the same enclosing group, if any. 
		 For example, in the pattern a(b(?i)c)d, only c is allowed to be case-insensitive. You can also force case 
		 sensitivity with (?-i).

	     The inline modifier can also contain pattern characters using the form (?i:abc). In this case, only those 
	     pattern characters inside the inline modifier's enclosing group are affected. This form does not capture text 
    	 By default, case-insensitive matching assumes that only characters in the US-ASCII 
    	 charset are being matched. Unicode-aware case-insensitive matching can be enabled by 
    	 specifying the UNICODE_CASE flag in conjunction with this flag. 
    	 Case-insensitive matching can also be enabled via the embedded flag expression (?i). 
    	 Specifying this flag may impose a slight performance penalty.*/
        if(!caseSensitve){
        	// Omitting UNICODE_CASE causes only US ASCII characters to be matched case insensitively
            // This is appropriate if you know beforehand that the subject string will only contain
            // US ASCII characters as it speeds up the pattern matching.
            options |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    	}
        if(dotAll){
        	// By default, the dot will not match line break characters.
            // Specify this option to make the dot match all characters, including line breaks
            options |= Pattern.DOTALL;
    	}
        if(multiLine){
        	// By default, the caret ^, dollar $ as well as \A, \z and \Z
            // only match at the start and the end of the string
            // Specify this option to make ^ and \A also match after line breaks in the string,
            // and make $, \z and \Z match before line breaks in the string
            options |= Pattern.MULTILINE;
        }
        LOGGER.trace("regex: {}", regEx);

		return Pattern.compile(regEx, options);
	}
    
 

    /*
	 * Finding Lines Containing or Not Containing Certain Words
	 * 
	 * If a line can meet any out of series of requirements, simply use
	 * alternation in the regular expression. ^.*\b(one|two|three)\b.*$
	 * Analyze this regular expression with RegexBuddy matches a
	 * complete line of text that contains any of the words "one", "two"
	 * or "three". The first backreference will contain the word the
	 * line actually contains. If it contains more than one of the
	 * words, then the last (rightmost) word will be captured into the
	 * first backreference. This is because the star is greedy. If we
	 * make the first star lazy, like in ^.*?\b(one|two|three)\b.*$,
	 * then the backreference will contain the first (leftmost) word.
	 * 
	 * If a line must satisfy all of multiple requirements, we need to
	 * use lookahead. ^(?=.*?\bone\b)(?=.*?\btwo\b)(?=.*?\bthree\b).*$
	 * Analyze this regular expression with RegexBuddy matches a
	 * complete line of text that contains all of the words "one", "two"
	 * and "three". Again, the anchors must match at the start and end
	 * of a line and the dot must not match line breaks. Because of the
	 * caret, and the fact that lookahead is zero-width, all of the
	 * three lookaheads are attempted at the start of the each line.
	 * Each lookahead will match any piece of text on a single line
	 * (.*?) followed by one of the words. All three must match
	 * successfully for the entire regex to match. Note that instead of
	 * words like \bword\b, you can put any regular expression, no
	 * matter how complex, inside the lookahead. Finally, .*$ causes the
	 * regex to actually match the line, after the lookaheads have
	 * determined it meets the requirements.
	 * 
	 * If your condition is that a line should not contain something,
	 * use negative lookahead. ^((?!regexp).)*$ Analyze this regular
	 * expression with RegexBuddy matches a complete line that does not
	 * match regexp. Notice that unlike before, when using positive
	 * lookahead, I repeated both the negative lookahead and the dot
	 * together. For the positive lookahead, we only need to find one
	 * location where it can match. But the negative lookahead must be
	 * tested at each and every character position in the line. We must
	 * test that regexp fails everywhere, not just somewhere.
	 * 
	 * Finally, you can combine multiple positive and negative
	 * requirements as follows:
	 * ^(?=.*?\bmust-have\b)(?=.*?\bmandatory\b)((?!avoid|illegal).)*$
	 * Analyze this regular expression with RegexBuddy. When checking
	 * multiple positive requirements, the .* at the end of the regular
	 * expression full of zero-width assertions made sure that we
	 * actually matched something. Since the negative requirement must
	 * match the entire line, it is easy to replace the .* with the
	 * negative test.
	 * 
	 * 
	 * Simple RegEx Tutorial
	 * 
	 * Regular Expression can be used in Content Filter conditions.
	 * 
	 * Regular Expressions can be extremely complex but they are very
	 * flexible and powerful and can be used to perform comparisons that
	 * cannot be done using the other checks available.
	 * 
	 * There follows some very basic examples of regular expression
	 * usage. For a complete description please visit
	 * www.regular-expressions.info. ^' and '$'
	 * 
	 * First of all, let's take a look at two special symbols: '^' and
	 * '$'. These symbols indicate the start and the end of a string,
	 * respectively:
	 * 
	 * "^The"
	 * 
	 * 
	 * matches any string that starts with "The".
	 * 
	 * "of despair$"
	 * 
	 * 
	 * matches a string that ends in with "of despair".
	 * 
	 * "^abc$"
	 * 
	 * 
	 * a string that starts and ends with "abc" - effectively an exact
	 * match comparison.
	 * 
	 * "notice"
	 * 
	 * 
	 * a string that has the text "notice" in it.
	 * 
	 * You can see that if you don't use either of these two characters,
	 * you're saying that the pattern may occur anywhere inside the
	 * string -- you're not "hooking" it to any of the edges. '*', '+',
	 * and '?'
	 * 
	 * In addition, the symbols '*', '+', and '?', denote the number of
	 * times a character or a sequence of characters may occur. What
	 * they mean is: "zero or more", "one or more", and "zero or one."
	 * Here are some examples:
	 * 
	 * "ab*"
	 * 
	 * 
	 * matches a string that has an a followed by zero or more b's
	 * ("ac", "abc", "abbc", etc.)
	 * 
	 * "ab+"
	 * 
	 * 
	 * same, but there's at least one b ("abc", "abbc", etc., but not
	 * "ac")
	 * 
	 * "ab?"
	 * 
	 * 
	 * there might be a single b or not ("ac", "abc" but not "abbc").
	 * 
	 * "a?b+$"
	 * 
	 * 
	 * a possible 'a' followed by one or more 'b's at the end of the
	 * string:
	 * 
	 * Matches any string ending with "ab", "abb", "abbb" etc. or "b",
	 * "bb" etc. but not "aab", "aabb" etc.
	 * 
	 * Braces { }
	 * 
	 * You can also use bounds, which appear inside braces and indicate
	 * ranges in the number of occurrences:
	 * 
	 * "ab{2}"
	 * 
	 * 
	 * matches a string that has an a followed by exactly two b's
	 * ("abb")
	 * 
	 * "ab{2,}"
	 * 
	 * 
	 * there are at least two b's ("abb", "abbbb", etc.)
	 * 
	 * "ab{3,5}"
	 * 
	 * 
	 * from three to five b's ("abbb", "abbbb", or "abbbbb")
	 * 
	 * Note that you must always specify the first number of a range
	 * (i.e., "{0,2}", not "{,2}"). Also, as you might have noticed, the
	 * symbols '*', '+', and '?' have the same effect as using the
	 * bounds "{0,}", "{1,}", and "{0,1}", respectively.
	 * 
	 * Now, to quantify a sequence of characters, put them inside
	 * parentheses:
	 * 
	 * "a(bc)*"
	 * 
	 * 
	 * matches a string that has an a followed by zero or more copies of
	 * the sequence "bc"
	 * 
	 * "a(bc){1,5}"
	 * 
	 * 
	 * one through five copies of "bc."
	 * 
	 * '|' OR operator
	 * 
	 * There's also the '|' symbol, which works as an OR operator:
	 * 
	 * "hi|hello"
	 * 
	 * 
	 * matches a string that has either "hi" or "hello" in it
	 * 
	 * "(b|cd)ef"
	 * 
	 * 
	 * a string that has either "bef" or "cdef"
	 * 
	 * "(a|b)*c"
	 * 
	 * 
	 * a string that has a sequence of alternating a's and b's ending in
	 * a c
	 * 
	 * ('.')
	 * 
	 * A period ('.') stands for any single character:
	 * 
	 * "a.[0-9]"
	 * 
	 * 
	 * matches a string that has an a followed by one character and a
	 * digit
	 * 
	 * "^.{3}$"
	 * 
	 * 
	 * a string with exactly 3 characters
	 * 
	 * Bracket expressions
	 * 
	 * specify which characters are allowed in a single position of a
	 * string:
	 * 
	 * "[ab]"
	 * 
	 * 
	 * matches a string that has either an a or a b (that's the same as
	 * "a|b")
	 * 
	 * "[a-d]"
	 * 
	 * 
	 * a string that has lowercase letters 'a' through 'd' (that's equal
	 * to "a|b|c|d" and even "[abcd]")
	 * 
	 * "^[a-zA-Z]"
	 * 
	 * 
	 * a string that starts with a letter
	 * 
	 * "[0-9]%"
	 * 
	 * 
	 * a string that has a single digit before a percent sign
	 * 
	 * ",[a-zA-Z0- 9]$"
	 * 
	 * 
	 * a string that ends in a comma followed by an alphanumeric
	 * character
	 * 
	 * You can also list which characters you DON'T want -- just use a
	 * '^' as the first symbol in a bracket expression (i.e.,
	 * "%[^a- zA-Z]%" matches a string with a character that is not a
	 * letter between two percent signs).
	 * 
	 * In order to be taken literally, you must escape the characters
	 * "^.[$()|*+?{\" with a backslash ('\'), as they have special meaning. On top of that, you must escape the backslash character itself in PHP3 strings, so, for instance, the regular expression "
	 * (\$|A)[0-9]+" would have the function call: ereg("(\\$|A)[0-9]+",
	 * $str) (what string does that validate?)
	 * 
	 * Just don't forget that bracket expressions are an exception to
	 * that rule--inside them, all special characters, including the
	 * backslash ('\'), lose their special powers (i.e., "[*\+?{}.]
	 * " matches exactly any of the characters inside the brackets). And, as the regex manual pages tell us: "
	 * To include a literal ']' in the list, make it the first character
	 * (following a possible '^'). To include a literal '-', make it the
	 * first or last character, or the second endpoint of a range."
	 * 
	 * 
	 * 
	 * 
	 * Every extended pattern is written as a parenthetical group with a
	 * question mark as the first character. The notation for the
	 * look-arounds is fairly mnemonic, but there are some other,
	 * experimental patterns that are similar, so it is important to get
	 * all the characters in the right order.
	 * 
	 *  
	 *  
	 *  
	 * 
	 */
	
	
	//	'^' start of a string
	//	'$' end of a string
	//	'.' stands for any single character
	//	'*' "zero or more"
	//	'+' "one or more"
	//	'?' "zero or one
	//	(?=pattern) is a positive look-ahead assertion
	//	(?!pattern) is anegative look-ahead assertion
	//	(?<=pattern) is a positive look-behind assertion
	//	(?<!pattern) is a negative look-behind assertion
	
	/*Grouping and Backreferences

	Place round brackets around multiple tokens to group them together. 
	You can then apply a quantifier to the group. E.g. Set(Value)? matches Set or SetValue.

	Round brackets create a capturing group. The above example has one group. 
	After the match, group number one will contain nothing if Set was matched or Value 
	if SetValue was matched. How to access the group's contents depends on the software or
	programming language you're using. Group zero always contains the entire regex match.*/
	
	
	/*If your condition is that a line should not contain something,
	 * use negative lookahead. ^((?!regexp).)*$ Analyze this regular
	 * expression with RegexBuddy matches a complete line that does not
	 * match regexp. Notice that unlike before, when using positive
	 * lookahead, I repeated both the negative lookahead and the dot
	 * together. For the positive lookahead, we only need to find one
	 * location where it can match. But the negative lookahead must be
	 * tested at each and every character position in the line. We must
	 * test that regexp fails everywhere, not just somewhere.*/
    public void main(String[] args) {
		try{
			
			LOGGER.info("{}", UB_REQUEST);
			LOGGER.info("{}", find(UB_REQUEST, "(opaque=\"[a-z]+(_)[a-z]+\")", true, true, true, 0));
			LOGGER.info("{}", find(UB_REQUEST, "(opaque=\"[a-z_]+\")", true, true, true, 0));
			LOGGER.info("{}", find(UB_REQUEST, "\"dynamic_opaque\"", true, true, true, 0));
			LOGGER.info("{}", find(UB_REQUEST, "^Authorization: Digest username=\"([0-9@.a-z]+)", true, true, true, 1));
			LOGGER.info("{}", find(UB_REQUEST, "^Content-Length:\\s([0-9]+)", true, true, true, 1));
			LOGGER.info("{}", find(UB_REQUEST, "GBA-service; ([0-9.]+)", true, true, true, 1));
			
			//LOGGER.info("{}", RegExServices.build().matches("Tests:   54 | Passed:  40 | Failed:  14", ROUNDUP_END_MARKER_REGEX));
			
			//LOGGER.info("{}", matches("[SUBTEST] it_io_subtest1:                 C12345  [PASS]\n", ROUNDUP_PARSER_REGEX2));
			//LOGGER.info("{}", findAllMap("[SUBTEST] it_io_subtest1:                 C12345  [PASS]\n", ROUNDUP_PARSER_REGEX2, true, true, true));
			
			
			//LOGGER.info("{}", regExTester("[SUBTEST] it_io_subtest1:                 C12345  [PASS]\n", ROUNDUP_PARSER_REGEX, true, true, true));
			
			
			//LOGGER.info("{}", regExTester("[SUBTEST] it io subtest1 C12345:      [PASS]\n", ROUNDUP_PARSER_REGEX2, true, true, true));
			
			//LOGGER.info("{}", regExTester("[TEST] simple2", ROUNDUP_PARSER_REGEX, true, true, true));
			
			//LOGGER.info("{}", regExTester(UB_REQUEST, "opaque=\"dynamic_opaque\"", true, true, true));
			
			
			
		//TODO this wants a colon at end...fix
			
			
			
			//LOGGER.info("{}", regExTester("[SUBTEST] it_snapshot_Expected_Failure_subtest20_enable_snapshot_on_attached_disk_C74454: [FAIL]", ROUNDUP_PARSER_REGEX, true, true, true));
			
			//LOGGER.info("{}", regExTester("[TEST] simple-test.sh over ridden default describe...", ROUNDUP_PARSER_REGEX, true, true, true));
			
			//LOGGER.info("{}", regExTester("[TEST] simple2", ROUNDUP_PARSER_REGEX, true, true, true));
			
			//LOGGER.info("{}", regExTester("[TEST] simple2 C12345", ROUNDUP_PARSER_REGEX, true, true, true));
			
			//LOGGER.info("{}", regExTester("[SUBTEST] it io subtest1 C12345:    [PASS]\n", ROUNDUP_PARSER_REGEX, true, true, true));
			
			//LOGGER.info("{}", regExTester("a b ", "[^:]+?", true, true, true));
			
			
			//LOGGER.info("{}", regExTester("[SUBTEST] it io subtest1 :      [PASS]\n", ROUNDUP_PARSER_REGEX2, true, true, true));
			
			//LOGGER.info("{}", RegExServices.build().test("0.1.3.4dhskfjhasjfhsjfhj", "((?:\\d+\\.?)+)(.*)"));
			
			
			
			//LOGGER.info("{}", RegExServices.build().test("0.1.3.4dhskfjhasjfhsjfhj", "((\\d+\\.?)+)(.*)"));
			
			
			//LOGGER.info("{}", find("0.1.3.4dhskfjhasjfhsjfhj", REGEX_VERSION_MATCH, true, false, false, 1));
			
			
			
			/*Log4JUtils.init("conf/log4j.properties");
			
			
			/*
			 *  	
			 *  
			 *  	****** AWESOME UTIL FOR TESTING REGEX'S *******
			 * 
			 * 		RegExr    http://www.gskinner.com/RegExr/
			 * 
			 * 
			 */


			/*
			 * find value of some variable in text
			 * 
			 */
			/*String data = "write: io=2048.0MB, bw=115133KB/s, iops=28783 , runt= 18215msec";
			
			//LOGGER.info("regEx: {}", VOLUME_REGEX);
			String iops = RegExServices.build().find(data, IOPS_REGEX , true, true, true, 1);
			LOGGER.info("iops: " + iops);
			
			
			
			
			//##################################
			//		see regExExamples.txt
			//##################################
			
			//String regEx = "^\\s*(TEST|SUBTEST):\\s+([^:]*):*\\s*(\\[(PASS|FAIL|SKIP)\\])?";
			//lookahead example
			//String regEx = "^\\s*(TEST|SUBTEST):\\s+([^:]*):*\\s*(?=\\[(PASS|FAIL|SKIP)\\])?";
			//regEx = "^\\s*(TEST|SUBTEST):\\s+([^:]*):*\\s*(?=(PASS|FAIL|SKIP)";
			//Non-capturing parentheses group the regex so you can apply regex operators, but do not capture anything and do not create backreferences.
			//String regEx = "^\\s*(TEST|SUBTEST):\\s+(?:storage testing io)(.*)";
			//String regEx = "(.*)(.*testing)";
			//non capturing solution
			//String regEx = "^\\s*(TEST|SUBTEST):\\s+([^:]*):*\\s*(?:\\[(PASS|FAIL|SKIP)\\])?";
			
			
			/*
			
			
			
				:* 0 or more :
				:? 0 or 1 :
				:+ 1 or more ?
				[^:]* any char that is not : 0 or more times
				(?:FOO|BAR) is a non capturing 
				^\\s* 0 or more starting white space
				\\d+ 1 0r more decimals
				\\\\d+\\.\\d+  will capture 33.4
				\\. catures a . char
				
				//^pVol_[0-9]+/TESTVOL_1377983131271  
				//pVol_5/TESTVOL_1377983131271                  229K  4.02T   229K  /pVol_5/TESTVOL_1377906811201
			
			
			*/
			
			/*
			  
			  					GREEDY by default....LAZY by definition
			
		    The standard quantifiers in regular expressions are greedy, meaning they match as much as they can, 
		    only giving back as necessary to match the remainder of the regex.

		    By using a lazy quantifier, the expression tries the minimal match first.
		    <em>Hello World</em>

		    You may think that <.+> (. means anything and + means repeated) would only match the <em> and the </em>, 
		    when in reality it will be very greedy, and go from the first < to the last >. This means it will 
		    match <em>Hello World</em> instead of what you wanted.

		    Making it lazy (<.+?>) will prevent this. By adding the ? after the +, we tell it to repeat as few times 

		    as possible, so the first > it comes across, is where we want to stop the matching.*/
			
			
			
			//QUICK EXAMPLE
			//		     ssn_version          : 0.4.114-devint
			/*String regEx2 =     "^\\s*ssn_version\\s*:\\s+(\\d+\\.\\d+)\\.(\\d+)(.*)";
			/*captures:
				GROUP(0):      ssn_version          : 0.4.89-devint
				GROUP(1): 0.4
				GROUP(2): 89
				GROUP(3): -devint*/	
			
			
			
			/*String regEx = "^\\s*(TEST|SUBTEST):\\s+([^:]*):*\\s*(?:\\[(PASS|FAIL|SKIP)\\])?";
			
			String line1 = " TEST: storage testing io";
			
	        
			String line2 = "  SUBTEST: it_io_subtest1:   [PASS]\n";
			

			Pattern pattern = Pattern.compile(regEx); 
	        
			
			Matcher beginMatcher = pattern.matcher(line2);
			
	        if(beginMatcher.find()){
	        	for(int i=0; i<beginMatcher.groupCount()+1; i++){
	        		System.err.println("GROUP(" + i + "): " + beginMatcher.group(i));
	        	}
	        }else{
	        	System.err.println("***************NO MATCH***************");
	        }

	        String s = "pVol_5/TESTVOL_1377906811201                  229K  4.02T   229K  /pVol_5/TESTVOL_1377906811201";
			
			//(?!bar) negative look ahead for bar
			//((?!bar).) negative look ahead on bar for a single char in the string
			//((?!bar).)* apply lookahed at each and every char in string
			
			//LOGGER.info("foobar".matches(   "^((?!bar).)*$")      );
			
			
			//LOGGER.info(matches("FOO", "foo", true));
			
			//LOGGER.info("foobar".matches(".*bar"));
			//LOGGER.info("foobar".matches("(?!barr)"));
			//replace("output/foo.txt", "poop", "poop2", true, true);
			//System.out.println(RegExServices.build().escapeNonAlphaNumeric("The Test &&&Harness is powerful"));
			//System.out.println(RegExServices.build().find("The Test Harness is powerful!", "Harness", true));
			//replace("support/testFiles/echo.bat", "hello", "foo", true, true);
			
			
			
			//String s = "hell,o\n";
			//LOGGER.info(s.replaceAll("(\n|,)" , ""));
			
			//LOGGER.info(splitQuotedArgs("a b \"c d e\"")[2]);
			
			//LOGGER.info(grepFile("support/csvFiles/PERSON_MATCH2.csv", ",NM,", true).size());
			//LOGGER.info(grepFile("C:/zoom/core/applications/dataservices/systemTest/data/listPull/csvFiles/groundTruth/zi_Test_Harness_All_Contacts_0_COMPANIES.csv", ",NC,", true).size());
			
			//LOGGER.info(grepFile("C:/_TEMP/zi_Test_Harness_All_Contacts_0_COMPANIES_2013-01-23.csv", ",M,", true).size());
			
			/*String data = "jhagdgaghsdh\n" +
					"FIRST_BLOCK_2013-01-25 08:59:46,766 INFO main com.zoominfo.dataservices.solr.HdfsSolrCsvCallback - Setting output path FullCircle/listpulls/CoMa_PriQs_0/input/lxssoltd01.xoominfo.com_2_remaining_mktMIKE\n" + 
					"2013-01-25 08:59:46,766 INFO main com.zoominfo.dataservices.solr.HdfsSolrCsvCallback - Setting output path FullCircle/listpulls/CoMa_PriQs_0/input/lxssoltd01.xoominfo.com_2_remaining_mkt\n" + 
					"PITONIAK\n" +
					"blahblahblahfoobar\n" +
					"dont collect me....\n" +
					"SECOND_BLOCK_2013-01-25 08:59:46,766 INFO main com.zoominfo.dataservices.solr.HdfsSolrCsvCallback - Setting output path FullCircle/listpulls/CoMa_PriQs_0/input/lxssoltd01.xoominfo.com_2_remaining_mktMIKE\n" + 
					"2013-01-25 08:59:46,766 INFO main com.zoominfo.dataservices.solr.HdfsSolrCsvCallback - Setting output path FullCircle/listpulls/CoMa_PriQs_0/input/lxssoltd01.xoominfo.com_2_remaining_mkt\n" + 
					"PITONIAK\n";*/
			
			//data = "CoMa_PriQs_0   CoMa_PriQs_0   CoMa_PriQs_0";
			//LOGGER.info(RegExServices.build().parseLogForExceptions("support/testFiles/fileWithException.txt"));
			
			//String data = FileServices.build().getFileContents("support/testFiles/fileWithException.txt");
			//System.err.println(RegExServices.build().parseStringForRegionMatches(data, "CoMa_Pri", "PITONIAK"));
			//System.err.println(RegExServices.build().parseStringForRegionMatches(data, "CoMa", "_Pri"));
			
			
			//System.err.println(RegExServices.build().parseLogForExceptions("support/testFiles/fileWithException.txt"));
			
		}catch(Exception e){
			LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
    }
    
    @Override
    protected RegExServices clone() throws CloneNotSupportedException{
        return (RegExServices)SerializationUtils.clone(this);
    }

}

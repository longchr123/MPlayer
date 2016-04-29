/*
 * Copyright (C) 2012 YIXIA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yixia.camera.demo.util;

import java.io.File;
import java.lang.reflect.Array;
import java.util.regex.Pattern;

import android.net.Uri;

public final class MediaUtils {
	public static final String[] EXTENSIONS;
	// http://www.fileinfo.com/filetypes/video
	public static final String[] VIDEO_EXTENSIONS = { "\\.264", "\\.3g2", "\\.3gp", "\\.3gp2", "\\.3gpp", "\\.3gpp2", "\\.3mm", "\\.3p2", "\\.60d", "\\.aep", "\\.ajp", "\\.amv", "\\.amx", "\\.arf", "\\.asf", "\\.asx", "\\.avb", "\\.avd", "\\.avi", "\\.avs", "\\.avs", "\\.axm", "\\.bdm", "\\.bdmv", "\\.bik", "\\.bin", "\\.bix", "\\.bmk", "\\.box", "\\.bs4", "\\.bsf", "\\.byu", "\\.camre", "\\.clpi", "\\.cpi", "\\.cvc", "\\.d2v", "\\.d3v", "\\.dat", "\\.dav", "\\.dce", "\\.dck", "\\.ddat", "\\.dif", "\\.dir", "\\.divx", "\\.dlx", "\\.dmb", "\\.dmsm", "\\.dmss", "\\.dnc", "\\.dpg", "\\.dream", "\\.dsy", "\\.dv", "\\.dv-avi", "\\.dv4", "\\.dvdmedia", "\\.dvr-ms", "\\.dvx", "\\.dxr", "\\.dzm", "\\.dzp", "\\.dzt", "\\.evo", "\\.eye", "\\.f4p", "\\.f4v", "\\.fbr", "\\.fbr", "\\.fbz", "\\.fcp", "\\.flc", "\\.flh", "\\.fli", "\\.flv", "\\.flx", "\\.gl", "\\.grasp", "\\.gts", "\\.gvi", "\\.gvp", "\\.hdmov", "\\.hkm", "\\.ifo", "\\.imovi", "\\.imovi", "\\.iva", "\\.ivf", "\\.ivr", "\\.ivs", "\\.izz", "\\.izzy", "\\.jts", "\\.lsf", "\\.lsx", "\\.m15", "\\.m1pg", "\\.m1v", "\\.m21", "\\.m21", "\\.m2a", "\\.m2p", "\\.m2t", "\\.m2ts", "\\.m2v", "\\.m4e", "\\.m4u", "\\.m4v", "\\.m75", "\\.meta", "\\.mgv", "\\.mj2", "\\.mjp", "\\.mjpg", "\\.mkv", "\\.mmv", "\\.mnv", "\\.mod", "\\.modd", "\\.moff", "\\.moi", "\\.moov", "\\.mov", "\\.movie", "\\.mp21", "\\.mp21", "\\.mp2v", "\\.mp4", "\\.mp4v", "\\.mpe", "\\.mpeg", "\\.mpeg4", "\\.mpf", "\\.mpg", "\\.mpg2", "\\.mpgin", "\\.mpl", "\\.mpls", "\\.mpv", "\\.mpv2", "\\.mqv", "\\.msdvd", "\\.msh", "\\.mswmm", "\\.mts", "\\.mtv", "\\.mvb", "\\.mvc", "\\.mvd", "\\.mve", "\\.mvp", "\\.mxf", "\\.mys", "\\.ncor", "\\.nsv", "\\.nvc", "\\.ogm", "\\.ogv", "\\.ogx", "\\.osp", "\\.par", "\\.pds", "\\.pgi", "\\.piv", "\\.playlist", "\\.pmf", "\\.prel", "\\.pro", "\\.prproj", "\\.psh", "\\.pva", "\\.pvr", "\\.pxv", "\\.qt", "\\.qtch", "\\.qtl", "\\.qtm", "\\.qtz", "\\.rcproject", "\\.rdb", "\\.rec", "\\.rm", "\\.rmd", "\\.rmp", "\\.rms", "\\.rmvb", "\\.roq", "\\.rp", "\\.rts", "\\.rts", "\\.rum", "\\.rv", "\\.sbk", "\\.sbt", "\\.scm", "\\.scm", "\\.scn", "\\.sec", "\\.seq", "\\.sfvidcap", "\\.smil", "\\.smk", "\\.sml", "\\.smv", "\\.spl", "\\.ssm", "\\.str", "\\.stx", "\\.svi", "\\.swf", "\\.swi", "\\.swt", "\\.tda3mt", "\\.tivo", "\\.tix", "\\.tod", "\\.tp", "\\.tp0", "\\.tpd", "\\.tpr", "\\.trp", "\\.ts", "\\.tvs", "\\.vc1", "\\.vcr", "\\.vcv", "\\.vdo", "\\.vdr", "\\.veg", "\\.vem", "\\.vf", "\\.vfw", "\\.vfz", "\\.vgz", "\\.vid", "\\.viewlet", "\\.viv", "\\.vivo", "\\.vlab", "\\.vob", "\\.vp3", "\\.vp6", "\\.vp7", "\\.vpj", "\\.vro", "\\.vsp", "\\.w32", "\\.wcp", "\\.webm", "\\.wm", "\\.wmd", "\\.wmmp", "\\.wmv", "\\.wmx", "\\.wp3", "\\.wpl", "\\.wtv", "\\.wvx", "\\.xfl", "\\.xvid", "\\.yuv", "\\.zm1", "\\.zm2", "\\.zm3", "\\.zmv" };
	// http://www.fileinfo.com/filetypes/audio
	public static final String[] AUDIO_EXTENSIONS = { "\\.4mp", "\\.669", "\\.6cm", "\\.8cm", "\\.8med", "\\.8svx", "\\.a2m", "\\.aa", "\\.aa3", "\\.aac", "\\.aax", "\\.abc", "\\.abm", "\\.ac3", "\\.acd", "\\.acd-bak", "\\.acd-zip", "\\.acm", "\\.act", "\\.adg", "\\.afc", "\\.agm", "\\.ahx", "\\.aif", "\\.aifc", "\\.aiff", "\\.ais", "\\.akp", "\\.al", "\\.alaw", "\\.all", "\\.amf", "\\.amr", "\\.ams", "\\.ams", "\\.aob", "\\.ape", "\\.apf", "\\.apl", "\\.ase", "\\.at3", "\\.atrac", "\\.au", "\\.aud", "\\.aup", "\\.avr", "\\.awb", "\\.band", "\\.bap", "\\.bdd", "\\.box", "\\.bun", "\\.bwf", "\\.c01", "\\.caf", "\\.cda", "\\.cdda", "\\.cdr", "\\.cel", "\\.cfa", "\\.cidb", "\\.cmf", "\\.copy", "\\.cpr", "\\.cpt", "\\.csh", "\\.cwp", "\\.d00", "\\.d01", "\\.dcf", "\\.dcm", "\\.dct", "\\.ddt", "\\.dewf", "\\.df2", "\\.dfc", "\\.dig", "\\.dig", "\\.dls", "\\.dm", "\\.dmf", "\\.dmsa", "\\.dmse", "\\.drg", "\\.dsf", "\\.dsm", "\\.dsp", "\\.dss", "\\.dtm", "\\.dts", "\\.dtshd", "\\.dvf", "\\.dwd", "\\.ear", "\\.efa", "\\.efe", "\\.efk", "\\.efq", "\\.efs", "\\.efv", "\\.emd", "\\.emp", "\\.emx", "\\.esps", "\\.f2r", "\\.f32", "\\.f3r", "\\.f4a", "\\.f64", "\\.far", "\\.fff", "\\.flac", "\\.flp", "\\.fls", "\\.frg", "\\.fsm", "\\.fzb", "\\.fzf", "\\.fzv", "\\.g721", "\\.g723", "\\.g726", "\\.gig", "\\.gp5", "\\.gpk", "\\.gsm", "\\.gsm", "\\.h0", "\\.hdp", "\\.hma", "\\.hsb", "\\.ics", "\\.iff", "\\.imf", "\\.imp", "\\.ins", "\\.ins", "\\.it", "\\.iti", "\\.its", "\\.jam", "\\.k25", "\\.k26", "\\.kar", "\\.kin", "\\.kit", "\\.kmp", "\\.koz", "\\.koz", "\\.kpl", "\\.krz", "\\.ksc", "\\.ksf", "\\.kt2", "\\.kt3", "\\.ktp", "\\.l", "\\.la", "\\.lqt", "\\.lso", "\\.lvp", "\\.lwv", "\\.m1a", "\\.m3u", "\\.m4a", "\\.m4b", "\\.m4p", "\\.m4r", "\\.ma1", "\\.mdl", "\\.med", "\\.mgv", "\\.mid", "\\.midi", "\\.miniusf", "\\.mka", "\\.mlp", "\\.mmf", "\\.mmm", "\\.mmp", "\\.mo3", "\\.mod", "\\.mp1", "\\.mp2", "\\.mp3", "\\.mpa", "\\.mpc", "\\.mpga", "\\.mpu", "\\.mp_", "\\.mscx", "\\.mscz", "\\.msv", "\\.mt2", "\\.mt9", "\\.mte", "\\.mti", "\\.mtm", "\\.mtp", "\\.mts", "\\.mus", "\\.mws", "\\.mxl", "\\.mzp", "\\.nap", "\\.nki", "\\.nra", "\\.nrt", "\\.nsa", "\\.nsf", "\\.nst", "\\.ntn", "\\.nvf", "\\.nwc", "\\.odm", "\\.oga", "\\.ogg", "\\.okt", "\\.oma", "\\.omf", "\\.omg", "\\.omx", "\\.ots", "\\.ove", "\\.ovw", "\\.pac", "\\.pat", "\\.pbf", "\\.pca", "\\.pcast", "\\.pcg", "\\.pcm", "\\.peak", "\\.phy", "\\.pk", "\\.pla", "\\.pls", "\\.pna", "\\.ppc", "\\.ppcx", "\\.prg", "\\.prg", "\\.psf", "\\.psm", "\\.ptf", "\\.ptm", "\\.pts", "\\.pvc", "\\.qcp", "\\.r", "\\.r1m", "\\.ra", "\\.ram", "\\.raw", "\\.rax", "\\.rbs", "\\.rcy", "\\.rex", "\\.rfl", "\\.rmf", "\\.rmi", "\\.rmj", "\\.rmm", "\\.rmx", "\\.rng", "\\.rns", "\\.rol", "\\.rsn", "\\.rso", "\\.rti", "\\.rtm", "\\.rts", "\\.rvx", "\\.rx2", "\\.s3i", "\\.s3m", "\\.s3z", "\\.saf", "\\.sam", "\\.sb", "\\.sbg", "\\.sbi", "\\.sbk", "\\.sc2", "\\.sd", "\\.sd", "\\.sd2", "\\.sd2f", "\\.sdat", "\\.sdii", "\\.sds", "\\.sdt", "\\.sdx", "\\.seg", "\\.seq", "\\.ses", "\\.sf", "\\.sf2", "\\.sfk", "\\.sfl", "\\.shn", "\\.sib", "\\.sid", "\\.sid", "\\.smf", "\\.smp", "\\.snd", "\\.snd", "\\.snd", "\\.sng", "\\.sng", "\\.sou", "\\.sppack", "\\.sprg", "\\.spx", "\\.sseq", "\\.sseq", "\\.ssnd", "\\.stm", "\\.stx", "\\.sty", "\\.svx", "\\.sw", "\\.swa", "\\.syh", "\\.syw", "\\.syx", "\\.td0", "\\.tfmx", "\\.thx", "\\.toc", "\\.tsp", "\\.txw", "\\.u", "\\.ub", "\\.ulaw", "\\.ult", "\\.ulw", "\\.uni", "\\.usf", "\\.usflib", "\\.uw", "\\.uwf", "\\.vag", "\\.val", "\\.vc3", "\\.vmd", "\\.vmf", "\\.vmf", "\\.voc", "\\.voi", "\\.vox", "\\.vpm", "\\.vqf", "\\.vrf", "\\.vyf", "\\.w01", "\\.wav", "\\.wav", "\\.wave", "\\.wax", "\\.wfb", "\\.wfd", "\\.wfp", "\\.wma", "\\.wow", "\\.wpk", "\\.wproj", "\\.wrk", "\\.wus", "\\.wut", "\\.wv", "\\.wvc", "\\.wve", "\\.wwu", "\\.xa", "\\.xa", "\\.xfs", "\\.xi", "\\.xm", "\\.xmf", "\\.xmi", "\\.xmz", "\\.xp", "\\.xrns", "\\.xsb", "\\.xspf", "\\.xt", "\\.xwb", "\\.ym", "\\.zvd", "\\.zvr" };
	public static final String[] SUBTRACK_EXTENSIONS = { ".srt", ".ssa", ".smi", ".txt", ".sub", ".ass" };
	public static final Pattern VIDEO_EXTENSIONS_PATTERN;
	public static final Pattern AUDIO_EXTENSIONS_PATTERN;
	public static final Pattern EXTENSIONS_PATTERN;
	public static final Pattern SUBTRACK_EXTENSIONS_PATTERN;
	static {
		EXTENSIONS = concat(VIDEO_EXTENSIONS, AUDIO_EXTENSIONS);
		EXTENSIONS_PATTERN = generatePattern(EXTENSIONS);
		VIDEO_EXTENSIONS_PATTERN = generatePattern(VIDEO_EXTENSIONS);
		AUDIO_EXTENSIONS_PATTERN = generatePattern(AUDIO_EXTENSIONS);
		SUBTRACK_EXTENSIONS_PATTERN = generatePattern(SUBTRACK_EXTENSIONS);
	}

	/**
	 * 数组工具类
	 * @author leichunguan
	 */
		public static <T> T[] concat(T[] A, T[] B) {
			final Class<?> typeofA = A.getClass().getComponentType();
			@SuppressWarnings("unchecked")
			T[] C = (T[]) Array.newInstance(typeofA, A.length + B.length);
			System.arraycopy(A, 0, C, 0, A.length);
			System.arraycopy(B, 0, C, A.length, B.length);

			return C;
		}
	
	public static boolean isVideoOrAudio(String url) {
		return EXTENSIONS_PATTERN.matcher(url.trim()).find();
	}

	public static boolean isVideoOrAudio(File file) {
		return EXTENSIONS_PATTERN.matcher(file.getName().trim()).find();
	}

	public static boolean isVideo(String url) {
		return VIDEO_EXTENSIONS_PATTERN.matcher(url.trim()).find();
	}

	public static boolean isVideo(File file) {
		return VIDEO_EXTENSIONS_PATTERN.matcher(file.getName().trim()).find();
	}

	public static boolean isAudio(String url) {
		return AUDIO_EXTENSIONS_PATTERN.matcher(url.trim()).find();
	}

	public static boolean isAudio(File file) {
		return AUDIO_EXTENSIONS_PATTERN.matcher(file.getName().trim()).find();
	}

	public static boolean isSubTrack(File file) {
		return SUBTRACK_EXTENSIONS_PATTERN.matcher(file.getName().trim()).find();
	}

	public static boolean isNative(String uri) {
		uri = Uri.decode(uri);
		return uri != null && (uri.startsWith("/") );
	}

	private static Pattern generatePattern(String[] args) {
		StringBuffer sb = new StringBuffer();
		for (String ext : args) {
			if (sb.length() > 0)
				sb.append("|");
			sb.append(ext);
		}
		return Pattern.compile("(" + sb.toString() + ")", Pattern.CASE_INSENSITIVE);
	}
}

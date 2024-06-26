/******************************************************************************
 * Copyright (C) 2012 Heng Sin Low                                            *
 * Copyright (C) 2012 Trek Global                 							  *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.compiere.print.util;

/**
 * Segment of swap file.<br/>
 * Each segment contains a list of file pointer (offset) pointing to block of equivalent size (except the last block).
 * @author hengsin
 */
public class SwapFileSegment {
	private final long[] offsets;
	private final int lastBlockSize;

	/**
	 * @param offsets
	 * @param lastSize
	 */
	public SwapFileSegment(long[] offsets, int lastSize)
	{
		this.offsets = offsets;
		this.lastBlockSize = lastSize;
	}

	/**
	 * Get offsets
	 * @return offsets
	 */
	public long[] getOffsets()
	{
		return offsets;
	}
	
	/**
	 * Get size of last block
	 * @return size of last block
	 */
	public int getLastBlockSize()
	{
		return lastBlockSize;
	}
}

/**
 * Eternity Keeper, a Pillars of Eternity save game editor.
 * Copyright (C) 2015 the authors.
 * <p>
 * Eternity Keeper is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * Eternity Keeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package uk.me.mantas.eternity.serializer;

import uk.me.mantas.eternity.environment.Environment;
import uk.me.mantas.eternity.factory.SharpSerializerFactory;
import uk.me.mantas.eternity.serializer.properties.Property;
import uk.me.mantas.eternity.serializer.properties.SimpleProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class DeserializedPackets {
	private List<Property> packets;
	private final SimpleProperty count;
	private final SharpSerializerFactory sharpSerializer;

	public DeserializedPackets (final List<Property> packets, final SimpleProperty count) {
		this.packets = packets;
		this.count = count;
		sharpSerializer = Environment.getInstance().factory().sharpSerializer();
	}

	public List<Property> getPackets () {
		return packets;
	}

	public void setPackets (final List<Property> packets) {
		this.packets = packets;
	}

	public SimpleProperty getCount () {
		return count;
	}

	public void reserialize (final File destinationFile) throws FileNotFoundException {
		final SharpSerializer serializer =
			sharpSerializer.forFile(destinationFile.getAbsolutePath());

		serializer.serialize(count);
		for (final Property property : packets) {
			serializer.serialize(property);
		}
	}
}

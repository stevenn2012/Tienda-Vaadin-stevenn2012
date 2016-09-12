package co.edu.usa.adf.TiendaVaadin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.ui.Notification;

import co.edu.usa.adf.datos.Producto;
import co.edu.usa.adf.datos.Venta;
import co.edu.usa.adf.Framework_Ancho_Fijo_Anotaciones.franfia;

public class ProductoService {

	private static ProductoService instance;
	private static final Logger LOGGER = Logger.getLogger(ProductoService.class.getName());
	private final HashMap<String, Producto> contacts = new HashMap<>();
	private ArrayList<Venta> venta = new ArrayList<Venta>();
	
	private ProductoService() {
	}

	public static ProductoService getInstance() {
		if (instance == null) {
			instance = new ProductoService();
			instance.ensureTestData();
		}
		return instance;
	}

	public synchronized List<Producto> findAll() {
		return findAll(null);
	}

	public synchronized List<Producto> findAll(String stringFilter) {
		ArrayList<Producto> arrayList = new ArrayList<>();
		for (Producto contact : contacts.values()) {
			try {
				
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())	|| contact.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(ProductoService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Producto>() {

			@Override
			public int compare(Producto o1, Producto o2) {
				return (int) (o1.getProductoId().compareTo(o2.getProductoId()));
			}
		});
		return arrayList;
	}

	public synchronized List<Producto> findAll(String stringFilter, int start, int maxresults) {
		ArrayList<Producto> arrayList = new ArrayList<>();
		for (Producto contact : contacts.values()) {
			try {
				boolean passesFilter = (stringFilter == null || stringFilter.isEmpty()) || contact.toString().toLowerCase().contains(stringFilter.toLowerCase());
				if (passesFilter) {
					arrayList.add(contact.clone());
				}
			} catch (CloneNotSupportedException ex) {
				Logger.getLogger(ProductoService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Collections.sort(arrayList, new Comparator<Producto>() {

			@Override
			public int compare(Producto o1, Producto o2) {
				return (int) (o1.getProductoId().compareTo(o2.getProductoId()));
			}
		});
		int end = start + maxresults;
		if (end > arrayList.size()) {
			end = arrayList.size();
		}
		return arrayList.subList(start, end);
	}

	public synchronized long count() {
		return contacts.size();
	}

	public boolean delete(Producto value) {
		boolean val = true;
		for (int i = 0; i < venta.size(); i++) {
			if(venta.get(i).getProductoId().equalsIgnoreCase(value.getProductoId())){
				val=false;
			}
		}
		if(val){
			System.out.println("Eliminando: "+value);
			if(contacts.get(value.getProductoId())!=null){
				contacts.remove(value.getProductoId());
				System.out.println("Dato eliminado con exito!");
				Notification.show("Producto Eliminado Con Exito!");
				return true;
			}else{
				Notification.show("No se encontro el producto a eliminar");
				return false;
			}
		}else{
			Notification.show("No se puede eliminar el producto mientras se encuentre en la tabla de venta!");
			return false;
		}
	}

	public synchronized boolean save(Producto entry) {
		boolean val = true;
		for (int i = 0; i < venta.size(); i++) {
			if(venta.get(i).getProductoId().equalsIgnoreCase(entry.getProductoId())){
				val=false;
			}
		}
		if(val){
			if (entry == null) {
				Notification.show("El producto es nulo");
				LOGGER.log(Level.SEVERE,"Producto is null.");
				return false;
			}
			try {
				entry = (Producto) entry.clone();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			contacts.put(entry.getProductoId(), entry);
			return true;
		}else{
			Notification.show("No se puede editar este elemento mientras se encuentre en la tabla de venta!");
			return false;
		}
	}

	public void ensureTestData() {
		System.out.println("Leyendo datos de persistencia...");
		if (findAll().isEmpty()) {
			try {
				ArrayList<Producto> productos = new franfia<Producto>("Datos/Descriptores/Descriptor_Producto.txt").leerArchivo();	
				for (int i = 0; i < productos.size(); i++) {
					save(productos.get(i));
				}
			} catch (Exception e) {
				System.out.println("Error cargando datos Franfia: "+e);
			}
		}
		System.out.println("Datos leidos con exito!");
		System.out.println("----------------->");
	}
	
	public void guardarDatos(){
		System.out.println("Guardando en persistencia...");
		try {
			ArrayList<Producto> productos = new ArrayList<Producto>();
			for (Producto contact : contacts.values()) {
				productos.add(contact);
			}
			franfia<Producto> prod = new franfia<Producto>("Datos/Descriptores/Descriptor_Producto.txt");
			Collections.sort(productos, new Comparator<Producto>() {
				@Override
				public int compare(Producto o1, Producto o2) {
					return (int) (o1.getProductoId().compareTo(o2.getProductoId()));
				}
			});
			
			prod.setDatos(productos);
			prod.escribirArchivo(false);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Error guardando");
		}
		System.out.println("Datos guardados en persistencia con exito!");
	}
	
	public int datosSize(){
		return contacts.size();
	}
	
	public void agregarProductoVenta(Producto producto){
		boolean exist = false;
		for (int i = 0; i < venta.size(); i++) {
			if(venta.get(i).getProductoId().equalsIgnoreCase(producto.getProductoId())){
				exist=true;
				if((venta.get(i).getCantidadVendida()+1)<=producto.getCantidadStock()){
					venta.get(i).setCantidadVendida(venta.get(i).getCantidadVendida()+1);
				}
			}
		}
		if(!exist){
			if(producto.getCantidadStock()>0){
				venta.add(new Venta("ABCDE12005", new Date(), producto.getProductoId(), 1, producto.getPrecioUnitario()));
			}else{
				Notification.show("No se puede agregar este producto. No hay mas en stock");
			}
		}
		System.out.println(venta);
	}
	
	public ArrayList<Venta> getVenta(){
		return venta;
	}
	
	public void cancelarVenta(){
		if(venta.size()>0){
			venta.removeAll(venta);
			Notification.show("Venta Cancelada con exito!");
		}else{
			Notification.show("EL carrito está Vacio");
		}
	}
	
	public void realizarVenta(){
		if(venta.size()>0){
			try {
				franfia<Venta> escribirVenta = new franfia<Venta>("Datos/Descriptores/Descriptor_Venta.txt");
				escribirVenta.leerArchivo();
				Date fecha = new Date();
				for (int i = 0; i < venta.size(); i++) {
					venta.get(i).setFecha(fecha);
					escribirVenta.add(venta.get(i));
					contacts.get(venta.get(i).getProductoId()).setCantidadStock(contacts.get(venta.get(i).getProductoId()).getCantidadStock()-venta.get(i).getCantidadVendida());
				}
				guardarDatos();
				escribirVenta.escribirArchivo(false);
				venta.removeAll(venta);
				Notification.show("Venta Realizada con exito!");
			} catch (Exception e) {
				System.out.println("Error Guardando venta"+ e);
			}
		}else{
			Notification.show("El Carrito está vacio");
		}
	}
	
	public void disminuirProducto(Venta venta){
		if(venta == null){
			Notification.show("NO Ha seleccionado ningun producto");
		}else{
			for (int i = 0; i < this.venta.size(); i++) {
				if(this.venta.get(i).getProductoId().equalsIgnoreCase(venta.getProductoId())){
					if(this.venta.get(i).getCantidadVendida()==1){
						this.venta.remove(i);
					}else{
						this.venta.get(i).setCantidadVendida(this.venta.get(i).getCantidadVendida()-1);
					}
				}
			}
		}
	}
	
	public void quitarProducto(Venta venta){
		if(venta == null){
			Notification.show("NO Ha seleccionado ningun producto");
		}else{
			for (int i = 0; i < this.venta.size(); i++) {
				if(this.venta.get(i).getProductoId().equalsIgnoreCase(venta.getProductoId())){
					this.venta.remove(i);
				}
			}
		}
	}
}

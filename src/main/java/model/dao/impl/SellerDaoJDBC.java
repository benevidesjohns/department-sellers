package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import entities.Department;
import entities.Seller;
import model.dao.SellerDao;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller obj) {
		PreparedStatement st = null;

		String query = "INSERT INTO seller (Name, Email, BirthDate, BaseSalary, DepartmentId) "
				+ "VALUES (?, ?, ?, ?, ?)";

		try {
			st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, Date.valueOf(obj.getBirthDate()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();

				if (rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
				}
				DB.closeResultSet(rs);
			} else {
				throw new DbException("Unexpected error! No rows affected!");
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Seller obj) {
		PreparedStatement st = null;

		String query = "UPDATE seller SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
				+ "WHERE id = ?";

		try {
			st = conn.prepareStatement(query);

			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, Date.valueOf(obj.getBirthDate()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			st.setInt(6, obj.getId());

			st.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		
		String query = "DELETE FROM seller WHERE Id = ?";

		try {
			st = conn.prepareStatement(query);

			st.setInt(1, id);
			st.executeUpdate();
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}

	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;

		String query = "SELECT seller.*,department.Name as DepName FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id WHERE seller.Id = ?";

		try {
			st = conn.prepareStatement(query);

			st.setInt(1, id);
			rs = st.executeQuery();

			if (rs.next()) {
				Department department = instantiateDepartment(rs);

				Seller seller = instantiateSeller(rs, department);

				return seller;
			}

			return null;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	private Seller instantiateSeller(ResultSet rs, Department department) throws SQLException {
		Seller seller = new Seller();

		seller.setId(rs.getInt("Id"));
		seller.setName(rs.getString("Name"));
		seller.setEmail(rs.getString("Email"));
		seller.setBaseSalary(rs.getDouble("BaseSalary"));
		seller.setBirthDate(rs.getDate("BirthDate").toLocalDate());
		seller.setDepartment(department);

		return seller;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department department = new Department();

		department.setId(rs.getInt("DepartmentId"));
		department.setName(rs.getString("DepName"));

		return department;
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;

		String query = "SELECT seller.*,department.Name as DepName FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id ORDER BY Name";

		try {
			st = conn.prepareStatement(query);

			rs = st.executeQuery();

			List<Seller> listSeller = new ArrayList<>();
			Map<Integer, Department> mapDep = new HashMap<>();

			while (rs.next()) {
				Department department = mapDep.get(rs.getInt("DepartmentId"));

				if (department == null) {
					department = instantiateDepartment(rs);
					mapDep.put(department.getId(), department);
				}

				Seller seller = instantiateSeller(rs, department);

				listSeller.add(seller);
			}

			return listSeller;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Seller> findByDepartment(Department dep) {
		PreparedStatement st = null;
		ResultSet rs = null;

		String query = "SELECT seller.*,department.Name as DepName FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id WHERE DepartmentId = ? ORDER BY Name";

		try {
			st = conn.prepareStatement(query);

			st.setInt(1, dep.getId());
			rs = st.executeQuery();

			List<Seller> listSeller = new ArrayList<>();
			Map<Integer, Department> mapDep = new HashMap<>();

			while (rs.next()) {
				Department department = mapDep.get(rs.getInt("DepartmentId"));

				if (department == null) {
					department = instantiateDepartment(rs);
					mapDep.put(department.getId(), department);
				}

				Seller seller = instantiateSeller(rs, department);

				listSeller.add(seller);
			}

			return listSeller;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

}

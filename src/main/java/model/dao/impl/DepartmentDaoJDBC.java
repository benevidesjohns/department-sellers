package model.dao.impl;

import java.sql.Connection;
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
import model.dao.DepartmentDao;

public class DepartmentDaoJDBC implements DepartmentDao {
	
	private Connection conn;
	
	public DepartmentDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Department obj) {
		PreparedStatement st = null;
		
		String query = "INSERT INTO department (Name) values (?)";
		
		try {
			st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			st.setString(1, obj.getName());
			
			int rowsAffected = st.executeUpdate();
			
			if(rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				
				if(rs.next()) {
					obj.setId(rs.getInt(1));
				}
				DB.closeResultSet(rs);
			} else {
				throw new DbException("Unexpected error! No rows affected!");
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

	@Override
	public void update(Department obj) {
		PreparedStatement st = null;
		
		String query = "UPDATE department SET Name = ? WHERE Id = ?";
		
		try {
			st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			st.setString(1, obj.getName());
			st.setInt(2, obj.getId());
			
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
		
		String query = "DELETE FROM department WHERE Id = ?";
		
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
	public Department findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		String query = "SELECT department.Id, department.Name AS DepName FROM department WHERE Id = ?";
		
		try {
			st = conn.prepareStatement(query);
			st.setInt(1, id);
			
			rs = st.executeQuery();
			
			if(rs.next()) {
				Department department = instantiateDepartment(rs);
				
				return department;
			}
			
			return null;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Department> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		String query = "SELECT department.Id, department.Name AS DepName FROM department";
		
		try {
			st = conn.prepareStatement(query);

			rs = st.executeQuery();
			
			List<Department> listDepartment = new ArrayList<>();
			while(rs.next()) {
				Department department = instantiateDepartment(rs);
				
				listDepartment.add(department);
			}
			
			return listDepartment;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
	
	@Override
	public List<Seller> getSellers(Integer departmentId) {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		String query = "SELECT seller.*, department.Name AS DepName FROM department INNER JOIN seller "
				+ "ON DepartmentId = department.Id WHERE DepartmentId = ?";
		
		try {
			st = conn.prepareStatement(query);
			
			st.setInt(1, departmentId);

			rs = st.executeQuery();
			
			List<Seller> listSeller = new ArrayList<>();
			Map<Integer, Department> mapDep = new HashMap<>();
			
			while(rs.next()) {
				Department department = mapDep.get(rs.getInt("DepartmentId"));
				
				if(department == null) {
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

		department.setId(rs.getInt("Id"));
		department.setName(rs.getString("DepName"));

		return department;
	}
}
